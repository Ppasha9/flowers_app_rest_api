package com.flowersapp.flowersappserver.services.tags

import com.flowersapp.flowersappserver.datatables.products.Tag
import com.flowersapp.flowersappserver.datatables.products.TagRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService {
    @Autowired
    private lateinit var tagRepository: TagRepository

    @Transactional
    fun getAllTags(): List<Tag> = tagRepository.findAll()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TagService::class.java)
    }
}