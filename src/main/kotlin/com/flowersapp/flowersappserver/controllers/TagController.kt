package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.services.tags.TagService
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
@RequestMapping("/api/tag")
class TagController {
    @Autowired
    private lateinit var tagService: TagService

    @GetMapping("")
    fun getAllTag(): ResponseEntity<Any> {
        logger.debug("Getting all tag")

        val tags = tagService.getAllTags()
        if (tags.isEmpty()) {
            return ResponseEntity("Tags not found", HttpStatus.NOT_FOUND)
        }

        val res = arrayListOf<String>()
        tags.forEach { res.add(it.code) }

        return ResponseEntity.ok(res)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TagController::class.java)
    }
}