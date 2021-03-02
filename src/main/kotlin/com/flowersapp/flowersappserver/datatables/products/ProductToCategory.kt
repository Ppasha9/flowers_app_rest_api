package com.flowersapp.flowersappserver.datatables.products

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_to_categories")
data class ProductToCategory(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_categories_gen")
    @SequenceGenerator(name = "products_to_categories_gen", sequenceName = "products_to_categories_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    var category: Category
)

interface ProductToCategoryRepository: JpaRepository<ProductToCategory, Long> {
    fun existsByCategoryCode(categoryCode: String): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByCategoryCodeAndProductId(categoryCode: String, productId: Long): Boolean

    fun findByCategoryCode(categoryCode: String): List<ProductToCategory>
    fun findByProductId(productId: Long): List<ProductToCategory>
    fun findByCategoryCodeAndProductId(categoryCode: String, productId: Long): ProductToCategory?
}
