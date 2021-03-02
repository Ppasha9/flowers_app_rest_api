package com.flowersapp.flowersappserver.services.categories

import com.flowersapp.flowersappserver.datatables.products.Category
import com.flowersapp.flowersappserver.datatables.products.CategoryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService {
    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Transactional
    fun getAllCategories(): List<Category> = categoryRepository.findAll()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CategoryService::class.java)
    }
}