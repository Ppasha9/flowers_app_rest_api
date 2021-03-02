package com.flowersapp.flowersappserver.datatables.products

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "products_to_pictures")
data class ProductToPicture(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pictures_gen")
    @SequenceGenerator(name = "pictures_gen", sequenceName = "pictures_seq")
    var id: Long? = null,

    @NotBlank
    var filename: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product
)

interface ProductToPictureRepository: JpaRepository<ProductToPicture, Long> {
    fun existsByProductId(productId: Long): Boolean
    fun existsByProductIdAndFilename(productId: Long, filename: String): Boolean

    fun findByProductId(productId: Long): List<ProductToPicture>
    fun findByProductIdAndFilename(productId: Long, filename: String): ProductToPicture?
}
