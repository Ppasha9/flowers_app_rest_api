package com.flowersapp.flowersappserver.datatables.products

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "compilations")
data class Compilation(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "compilations_gen")
    @SequenceGenerator(name = "compilations_gen", sequenceName = "compilations_seq")
    var id: Long? = null,

    var name: String = ""
)

interface CompilationRepository: JpaRepository<Compilation, Long> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): Compilation?
}

@Entity
@Table(name = "compilations_to_pictures")
data class CompilationToPicture(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "compilations_to_pictures_gen")
    @SequenceGenerator(name = "compilations_to_pictures_gen", sequenceName = "compilations_to_pictures_seq")
    var id: Long? = null,

    @NotBlank
    var filename: String,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compilation_id")
    var compilation: Compilation
)

interface CompilationToPictureRepository: JpaRepository<CompilationToPicture, Long> {
    fun existsByCompilationId(compilationId: Long): Boolean
    fun existsByCompilationIdAndFilename(compilationId: Long, filename: String): Boolean

    fun findByCompilationId(compilationId: Long): CompilationToPicture?
    fun findByCompilationIdAndFilename(compilationId: Long, filename: String): CompilationToPicture?
}

@Entity
@Table(name = "products_to_compilations")
data class ProductToCompilation(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_to_compilations_gen")
    @SequenceGenerator(name = "products_to_compilations_gen", sequenceName = "products_to_compilations_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compilation_id")
    var compilation: Compilation
)

interface ProductToCompilationRepository: JpaRepository<ProductToCompilation, Long> {
    fun existsByProductId(productId: Long): Boolean
    fun existsByCompilationId(compilationId: Long): Boolean
    fun existsByProductIdAndCompilationId(productId: Long, compilationId: Long): Boolean

    fun findByProductId(productId: Long): List<ProductToCompilation>
    fun findByCompilationId(compilationId: Long): List<ProductToCompilation>
    fun findByProductIdAndCompilationId(productId: Long, compilationId: Long): ProductToCompilation
}