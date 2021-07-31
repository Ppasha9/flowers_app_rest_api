package com.flowersapp.flowersappserver.datatables.products

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "products_to_flowers")
data class ProductToFlower(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_flowers_gen")
    @SequenceGenerator(name = "products_to_flowers_gen", sequenceName = "products_to_flowers_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_code")
    var flower: Flower
)

interface ProductToFlowerRepository: JpaRepository<ProductToFlower, Long> {
    fun existsByFlowerCode(flowerCode: String): Boolean
    fun existsByProductId(productId: Long): Boolean
    fun existsByFlowerCodeAndProductId(flowerCode: String, productId: Long): Boolean

    fun findByFlowerCode(flowerCode: String): List<ProductToFlower>
    fun findByProductId(productId: Long): List<ProductToFlower>
    fun findByFlowerCodeAndProductId(flowerCode: String, productId: Long): ProductToFlower?
}