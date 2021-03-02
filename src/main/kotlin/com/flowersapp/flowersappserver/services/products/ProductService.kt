package com.flowersapp.flowersappserver.services.products

import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.forms.products.*
import com.sun.org.apache.xpath.internal.operations.Bool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class ProductService {
    @Autowired
    private lateinit var productRepository: ProductRepository
    @Autowired
    private lateinit var productToCategoryRepository: ProductToCategoryRepository
    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Transactional
    fun findById(id: Long): Optional<Product> = productRepository.findById(id)

    fun existsById(id: Long): Boolean = productRepository.existsById(id)

    @Transactional
    fun filter(limit: Int?, substring: String?, minPrice: Int?, maxPrice: Int?, category: String?, groupNum: Int?): List<Product> {
        logger.debug("Filtering products in service")

        val products = productRepository.findByLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
            limit = limit,
            substring = substring,
            minPrice = minPrice,
            maxPrice = maxPrice,
            category = category,
            groupNum = groupNum
        )

        if (limit != null && groupNum != null) {
            val newProducts = arrayListOf<Product>()
            for (i in (limit * (groupNum - 1)) until products.count()) {
                newProducts.add(products[i])
            }

            return newProducts
        }

        return products
    }

    fun getCuttedForm(product: Product): ProductCuttedForm {
        return ProductCuttedForm(
            id = product.id!!,
            name = product.name,
            price = product.price
        )
    }

    fun getFullForm(product: Product): ProductFullForm {
        val date = if (product.addDate != null) product.addDate!! else OffsetDateTime.now()
        val res = ProductFullForm(
            id = product.id!!,
            name = product.name,
            description = product.description,
            price = product.price,
            addDate = date,
            categories = arrayListOf()
        )

        productToCategoryRepository.findByProductId(productId = product.id!!).forEach {
            res.categories.add(it.category.code)
        }

        return res
    }

    @Transactional
    fun createFrom(productForm: ProductCreateForm): String? {
        if (productRepository.existsByName(productForm.name)) {
            return "Product with name ${productForm.name} already exists."
        }

        val product = Product(
            name = productForm.name,
            description = productForm.description,
            price = productForm.price,
            addDate = OffsetDateTime.now()
        )
        productRepository.saveAndFlush(product)

        productForm.categories?.forEach {
            if (!categoryRepository.existsByCode(it)) {
                return "Category $it doesn't exist"
            }

            productToCategoryRepository.save(ProductToCategory(
                product = product,
                category = categoryRepository.findByCode(it)!!
            ))
        }

        return null
    }

    @Transactional
    fun patchWith(productId: Long, productForm: ProductCreateForm): String? {
        val productOptional = productRepository.findById(productId)
        if (!productOptional.isPresent) {
            return "There is not product with id $productId"
        }

        val product = productOptional.get()

        if (product.name != productForm.name && productRepository.existsByName(productForm.name)) {
            return "There is already a product with name $productForm.name"
        }
        product.name = productForm.name
        product.price = productForm.price
        product.description = productForm.description
        productRepository.save(product)

        val currTags = productToCategoryRepository.findByProductId(productId)
        currTags.forEach {
            if (!productForm.categories?.contains(it.category.code)!!) {
                productToCategoryRepository.delete(it)
            }
        }

        productForm.categories?.forEach {
            if (!categoryRepository.existsByCode(it)) {
                return "Category $it doesn't exist"
            }

            if (!productToCategoryRepository.existsByCategoryCodeAndProductId(it, productId)) {
                productToCategoryRepository.save(ProductToCategory(
                    product = product,
                    category = categoryRepository.findByCode(it)!!
                ))
            }
        }

        return null
    }

    @Transactional
    fun getOneProductForEachCategory(): OneProductPerCategoryCuttedForm {
        val res = OneProductPerCategoryCuttedForm(elems = arrayListOf())

        categoryRepository.findAll().forEach {
            if (productToCategoryRepository.existsByCategoryCode(it.code)) {
                val productToCategoryForm = OneProductToCategoryCutted(
                    category = it.code,
                    product = getCuttedForm(productToCategoryRepository.findByCategoryCode(it.code)[0].product)
                )

                res.elems.add(productToCategoryForm)
            }
        }

        return res
    }

    @Transactional
    fun getMaxPriceByCategory(category: String): Double? {
        return productRepository.findMaxPriceByCategoryNative(category)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)
    }
}