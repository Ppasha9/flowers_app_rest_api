package com.flowersapp.flowersappserver.datatables.products

import com.flowersapp.flowersappserver.constants.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "products")
data class Product(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_gen")
    @SequenceGenerator(name = "products_gen", sequenceName = "products_seq")
    var id: Long? = null,

    @Column(unique = true)
    @NotEmpty
    var name: String = "",

    @NotEmpty
    @Column(columnDefinition = "TEXT")
    var description: String = "",

    var price: Double = 0.0,

    @Column(name = "add_date")
    var addDate: OffsetDateTime? = null
)

interface ProductRepository: JpaRepository<Product, Long>, CustomProductRepository {
    fun existsByName(name: String): Boolean
}

interface CustomProductRepository {
    fun findByLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
        limit: Int?,
        substring: String?,
        minPrice: Int?,
        maxPrice: Int?,
        category: String?,
        groupNum: Int?
    ): List<Product>

    fun findMaxPriceByCategoryNative(tag: String): Double?
}

class CustomProductRepositoryImpl: CustomProductRepository {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findByLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
        limit: Int?,
        substring: String?,
        minPrice: Int?,
        maxPrice: Int?,
        category: String?,
        groupNum: Int?
    ): List<Product> {
        var isFirst = true
        var query = """
            select
                ${Constants.POSTGRES_SCHEME}.products.id,
                ${Constants.POSTGRES_SCHEME}.products.name,
                ${Constants.POSTGRES_SCHEME}.products.description,
                ${Constants.POSTGRES_SCHEME}.products.price,
                ${Constants.POSTGRES_SCHEME}.products.add_date 
            from ${Constants.POSTGRES_SCHEME}.products
        """

        if (category != null) {
            query += """
            join ${Constants.POSTGRES_SCHEME}.products_to_categories
            on
                category_code = '$category' and product_id = products.id
            """
        }

        if (substring != null || minPrice != null || maxPrice != null) {
            query += """
            where
            """

        }

        if (substring != null) {
            var tmp = ""
            if (!isFirst) {
                tmp = "and "
            } else {
                isFirst = false
            }

            query += """
                $tmp(${Constants.POSTGRES_SCHEME}.products.name like '%$substring%')
            """
        }

        if (minPrice != null) {
            var tmp = ""
            if (!isFirst) {
                tmp = "and "
            } else {
                isFirst = false
            }

            query += """
                $tmp(${Constants.POSTGRES_SCHEME}.products.price >= $minPrice)
            """
        }

        if (maxPrice != null) {
            var tmp = ""
            if (!isFirst) {
                tmp = "and "
            } else {
                isFirst = false
            }

            query += """
                $tmp(${Constants.POSTGRES_SCHEME}.products.price <= $maxPrice)
            """
        }

        query += """
            order by ${Constants.POSTGRES_SCHEME}.products.id desc
        """

        if (limit != null) {
            if (groupNum != null) {
                val newLimit = limit * groupNum
                query += """
            limit $newLimit
                """
            } else {
                query += """
            limit $limit 
                """
            }
        }

        return entityManager.createNativeQuery(query, Product::class.java).resultList as List<Product>
    }

    override fun findMaxPriceByCategoryNative(category: String): Double? {
        val query = """
            select max(${Constants.POSTGRES_SCHEME}.products.price) from ${Constants.POSTGRES_SCHEME}.products
            join ${Constants.POSTGRES_SCHEME}.products_to_categories
            on
                category_code = '$category' and product_id = products.id
        """

        val list = entityManager.createNativeQuery(query).resultList as List<Double>
        return if (list.isEmpty()) null else list[0]
    }
}
