package com.flowersapp.flowersappserver.datatables.products

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_to_tags")
data class ProductToTag(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_tags_gen")
    @SequenceGenerator(name = "products_to_tags_gen", sequenceName = "products_to_tags_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_code")
    var tag: Tag
)

interface ProductToTagRepository: JpaRepository<ProductToTag, Long> {
    fun existsByTagCode(tagCode: String): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByTagCodeAndProductId(tagCode: String, productId: Long): Boolean

    fun findByTagCode(tagCode: String): List<ProductToTag>
    fun findByProductId(productId: Long): List<ProductToTag>
    fun findByTagCodeAndProductId(tagCode: String, productId: Long): ProductToTag?
}
