package com.flowersapp.flowersappserver.services.products

import com.flowersapp.flowersappserver.datatables.products.*
import com.flowersapp.flowersappserver.datatables.users.User
import com.flowersapp.flowersappserver.datatables.users.UserToFavouriteProduct
import com.flowersapp.flowersappserver.datatables.users.UserToFavouriteProductRepository
import com.flowersapp.flowersappserver.forms.products.*
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private lateinit var productToTagRepository: ProductToTagRepository
    @Autowired
    private lateinit var productToFlowerRepository: ProductToFlowerRepository
    @Autowired
    private lateinit var categoryRepository: CategoryRepository
    @Autowired
    private lateinit var tagRepository: TagRepository
    @Autowired
    private lateinit var flowerRepository: FlowerRepository
    @Autowired
    private lateinit var userToFavouriteProductRepository: UserToFavouriteProductRepository
    @Autowired
    private lateinit var userService: UserService

    @Transactional
    fun findById(id: Long): Optional<Product> = productRepository.findById(id)

    fun existsById(id: Long): Boolean = productRepository.existsById(id)

    @Transactional
    fun filter(limit: Int?, substring: String?, minPrice: Int?, maxPrice: Int?, category: String?, groupNum: Int?, tags: String?, flowers: String?): List<Product> {
        logger.debug("Filtering products in service")

        val products = productRepository.findByLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
            limit = limit,
            substring = substring,
            minPrice = minPrice,
            maxPrice = maxPrice,
            category = category,
            groupNum = groupNum,
            tags = tags,
            flowers = flowers
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

    fun isProductFavouriteForCurrentUser(productId: Long): Boolean {
        val currUser = userService.getCurrentAuthorizedUser()
        var isFavourite = false

        if (currUser != null) {
            isFavourite = userToFavouriteProductRepository.existsByUserCodeAndProductId(currUser.code, productId)
        }

        logger.debug("isFavourite: $isFavourite")
        return isFavourite
    }

    fun getCuttedForm(product: Product): ProductCuttedForm {
        return ProductCuttedForm(
            id = product.id!!,
            name = product.name,
            price = product.price,
            productFavouriteForUser = isProductFavouriteForCurrentUser(product.id!!)
        )
    }

    fun getFullForm(product: Product): ProductFullForm {
        val date = if (product.addDate != null) product.addDate!! else OffsetDateTime.now()
        val res = ProductFullForm(
            id = product.id!!,
            name = product.name,
            content = product.content,
            size = product.size,
            height = product.height,
            diameter = product.diameter,
            price = product.price,
            productFavouriteForUser = isProductFavouriteForCurrentUser(product.id!!),
            addDate = date,
            categories = arrayListOf(),
            tags = arrayListOf(),
            flowers = arrayListOf()
        )

        productToCategoryRepository.findByProductId(productId = product.id!!).forEach {
            res.categories.add(it.category.code)
        }

        productToTagRepository.findByProductId(productId = product.id!!).forEach {
            res.tags.add(it.tag.code)
        }

        productToFlowerRepository.findByProductId(productId = product.id!!).forEach {
            res.flowers.add(it.flower.code)
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
            content = productForm.content,
            size = productForm.size,
            height = productForm.height,
            diameter = productForm.diameter,
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

        productForm.tags?.forEach {
            if (!tagRepository.existsByCode(it)) {
                return "Tag $it doesn't exist"
            }

            productToTagRepository.save(ProductToTag(
                product = product,
                tag = tagRepository.findByCode(it)!!
            ))
        }

        productForm.flowers?.forEach {
            if (!flowerRepository.existsByCode(it)) {
                return "Flower $it doesn't exist"
            }

            productToFlowerRepository.save(ProductToFlower(
                product = product,
                flower = flowerRepository.findByCode(it)!!
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
        product.content = productForm.content
        product.size = productForm.size
        product.height = productForm.height
        product.diameter = productForm.diameter
        productRepository.save(product)

        val currCategories = productToCategoryRepository.findByProductId(productId)
        currCategories.forEach {
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

        val currTags = productToTagRepository.findByProductId(productId)
        currTags.forEach {
            if (!productForm.tags?.contains(it.tag.code)!!) {
                productToTagRepository.delete(it)
            }
        }

        productForm.tags?.forEach {
            if (!tagRepository.existsByCode(it)) {
                return "Tag $it doesn't exist"
            }

            if (!productToTagRepository.existsByTagCodeAndProductId(it, productId)) {
                productToTagRepository.save(ProductToTag(
                    product = product,
                    tag = tagRepository.findByCode(it)!!
                ))
            }
        }

        val currFlowers = productToFlowerRepository.findByProductId(productId)
        currFlowers.forEach {
            if (!productForm.flowers?.contains(it.flower.code)!!) {
                productToFlowerRepository.delete(it)
            }
        }

        productForm.flowers?.forEach {
            if (!flowerRepository.existsByCode(it)) {
                return "Flower $it doesn't exist"
            }

            if (!productToFlowerRepository.existsByFlowerCodeAndProductId(it, productId)) {
                productToFlowerRepository.save(ProductToFlower(
                    product = product,
                    flower = flowerRepository.findByCode(it)!!
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

    @Transactional
    fun addProductToFavouriteForUser(productId: Long, user: User): String? {
        val productOptional = productRepository.findById(productId)
        if (!productOptional.isPresent) {
            return "There is no product with id $productId"
        }

        if (!userToFavouriteProductRepository.existsByUserCodeAndProductId(user.code, productId)) {
            userToFavouriteProductRepository.saveAndFlush(UserToFavouriteProduct(userCode = user.code, productId = productId))
        }

        return null
    }

    @Transactional
    fun removeProductFromFavouriteForUser(productId: Long, user: User): String? {
        val productOptional = productRepository.findById(productId)
        if (!productOptional.isPresent) {
            return "There is no product with id $productId"
        }

        if (userToFavouriteProductRepository.existsByUserCodeAndProductId(user.code, productId)) {
            userToFavouriteProductRepository.delete(userToFavouriteProductRepository.findByUserCodeAndProductId(user.code, productId)!!)
        }

        return null
    }

    @Transactional
    fun getAllFavouriteProductsForUser(user: User): List<Product> {
        val res = userToFavouriteProductRepository.findByUserCode(user.code)
        return res.map { productRepository.findById(it.productId!!).get() }.toList()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)
    }
}