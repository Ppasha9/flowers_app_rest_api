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
    var content: String = "",

    var size: String = "",

    var height: Double,

    var diameter: Double,

    var price: Double = 0.0,

    @Column(name = "add_date")
    var addDate: OffsetDateTime? = null
)

interface ProductRepository: JpaRepository<Product, Long>, CustomProductRepository {
    fun findByName(name: String): Product?
    fun existsByName(name: String): Boolean
}

interface CustomProductRepository {
    fun findByRangeAndLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
        limit: Int?,
        substring: String?,
        minPrice: Int?,
        maxPrice: Int?,
        category: String?,
        groupNum: Int?,
        tags: String?,
        flowers: String?
    ): List<Product>

    fun findMaxPriceByCategoryNative(category: String): Double?
}

class CustomProductRepositoryImpl: CustomProductRepository {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findByRangeAndLimitAndSubstringAndMinPriceAndMaxPriceAndCategoryNative(
        limit: Int?,
        substring: String?,
        minPrice: Int?,
        maxPrice: Int?,
        category: String?,
        groupNum: Int?,
        tags: String?,
        flowers: String?
    ): List<Product> {
        var isFirst = true
        var query = """
            select
                ${Constants.POSTGRES_SCHEME}.products.id,
                ${Constants.POSTGRES_SCHEME}.products.name,
                ${Constants.POSTGRES_SCHEME}.products.content,
                ${Constants.POSTGRES_SCHEME}.products.size,
                ${Constants.POSTGRES_SCHEME}.products.height,
                ${Constants.POSTGRES_SCHEME}.products.diameter,
                ${Constants.POSTGRES_SCHEME}.products.price,
                ${Constants.POSTGRES_SCHEME}.products.add_date 
            from ${Constants.POSTGRES_SCHEME}.products
        """

        if (category != null) {
            query += """
            join ${Constants.POSTGRES_SCHEME}.products_to_categories
            on
                ${Constants.POSTGRES_SCHEME}.products_to_categories.category_code = '$category' and ${Constants.POSTGRES_SCHEME}.products_to_categories.product_id = ${Constants.POSTGRES_SCHEME}.products.id
            """
        }

        if (tags != null) {
            val tagsStrs = tags.split(";")

            query += """
            join ${Constants.POSTGRES_SCHEME}.products_to_tags
            on 
            """

            tagsStrs.forEachIndexed { index, s ->
                run {
                    query += """
                ${Constants.POSTGRES_SCHEME}.products_to_tags.tag_code = '$s' and ${Constants.POSTGRES_SCHEME}.products_to_tags.product_id = ${Constants.POSTGRES_SCHEME}.products.id
                    """

                    if (index < tagsStrs.size - 1) {
                        query += """
                or
                        """
                    }
                }
            }
        }

        if (flowers != null) {
            val flowersStrs = flowers.split(";")

            query += """
            join ${Constants.POSTGRES_SCHEME}.products_to_flowers
            on 
            """

            flowersStrs.forEachIndexed { index, s ->
                run {
                    query += """
                ${Constants.POSTGRES_SCHEME}.products_to_flowers.flower_code = '$s' and ${Constants.POSTGRES_SCHEME}.products_to_flowers.product_id = ${Constants.POSTGRES_SCHEME}.products.id
                    """

                    if (index < flowersStrs.size - 1) {
                        query += """
                or
                        """
                    }
                }
            }
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
