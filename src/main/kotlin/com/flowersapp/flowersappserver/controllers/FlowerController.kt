package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.services.flowers.FlowerService
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
@RequestMapping("/api/flower")
class FlowerController {
    @Autowired
    private lateinit var flowerService: FlowerService

    @GetMapping("")
    fun getAllFlower(): ResponseEntity<Any> {
        logger.debug("Getting all flowers")

        val flowers = flowerService.getAllFlowers()
        if (flowers.isEmpty()) {
            return ResponseEntity("Flowers not found", HttpStatus.NOT_FOUND)
        }

        val res = arrayListOf<String>()
        flowers.forEach { res.add(it.code) }

        return ResponseEntity.ok(res)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FlowerController::class.java)
    }
}