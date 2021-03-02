package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.services.categories.CategoryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/category")
class CategoryController {
    @Autowired
    private lateinit var categoryService: CategoryService

    @GetMapping("")
    fun getAllCategories(): ResponseEntity<Any> {
        logger.debug("Getting all categories")

        val tags = categoryService.getAllCategories()
        if (tags.isEmpty()) {
            return ResponseEntity("Categories not found", HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok(tags)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CategoryController::class.java)
    }
}