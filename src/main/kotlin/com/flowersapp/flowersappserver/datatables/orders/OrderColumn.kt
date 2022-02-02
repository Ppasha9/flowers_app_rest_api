package com.flowersapp.flowersappserver.datatables.orders

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "orders_cards")
data class OrderCard(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_cards_gen")
    @SequenceGenerator(name = "orders_cards_gen", sequenceName = "orders_cards_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order
)

@Entity
@Table(name = "orders_columns")
data class OrderColumn(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_columns_gen")
    @SequenceGenerator(name = "orders_columns_gen", sequenceName = "orders_columns_seq")
    var id: Long? = null,

    @Column(unique = true)
    var name: String = ""
)

@Entity
@Table(name = "orders_cards_to_orders_columns")
data class OrderCardToOrderColumn(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_cards_to_orders_columns_gen")
    @SequenceGenerator(name = "orders_cards_to_orders_columns_gen", sequenceName = "orders_cards_to_orders_columns_seq")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_card_id")
    var orderCard: OrderCard,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_column_id")
    var orderColumn: OrderColumn
)

interface OrderCardRepository: JpaRepository<OrderCard, Long> {
    fun findByOrderId(orderId: Long): OrderCard?
    fun findTopByOrderByIdDesc(): OrderCard?
}

interface OrderColumnRepository: JpaRepository<OrderColumn, Long> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): OrderColumn?
}

interface OrderCardToOrderColumnRepository: JpaRepository<OrderCardToOrderColumn, Long> {
    fun findByOrderCardId(orderCardId: Long): OrderCardToOrderColumn?
}