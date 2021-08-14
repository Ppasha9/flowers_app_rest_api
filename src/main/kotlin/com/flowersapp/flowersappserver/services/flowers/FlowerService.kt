package com.flowersapp.flowersappserver.services.flowers

import com.flowersapp.flowersappserver.datatables.products.Flower
import com.flowersapp.flowersappserver.datatables.products.FlowerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlowerService {
    @Autowired
    private lateinit var flowerRepository: FlowerRepository

    @Transactional
    fun getAllFlowers(): List<Flower> = flowerRepository.findAll()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FlowerService::class.java)
    }
}