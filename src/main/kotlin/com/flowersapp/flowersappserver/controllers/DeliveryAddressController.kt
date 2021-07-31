package com.flowersapp.flowersappserver.controllers

import com.flowersapp.flowersappserver.constants.Constants
import com.flowersapp.flowersappserver.forms.delivery_address.DeliveryAddressForm
import com.flowersapp.flowersappserver.services.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/delivery_address")
class DeliveryAddressController {
    @Autowired
    private lateinit var userService: UserService

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PostMapping("")
    fun addDeliveryAddressToUser(@Valid @RequestBody form: DeliveryAddressForm?): ResponseEntity<Any> {
        logger.debug("Adding new delivery address for current user")

        if (form == null) {
            return ResponseEntity("Bad request", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        val err = userService.addDeliveryAddress(form, currUser)
        return if (err == null) {
            ResponseEntity.ok("Address was successfully added")
        } else {
            ResponseEntity(err, HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @PatchMapping("")
    fun editDeliveryAddressForUser(@Valid @RequestBody form: DeliveryAddressForm?): ResponseEntity<Any> {
        logger.debug("Patching delivery address for current user")

        if (form == null) {
            return ResponseEntity("Bad request", HttpStatus.BAD_REQUEST)
        }

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        val err = userService.editDeliveryAddress(form, currUser)
        return if (err == null) {
            ResponseEntity.ok("Address was successfully edited")
        } else {
            ResponseEntity(err, HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @DeleteMapping("")
    fun deleteDeliveryAddressForUser(@RequestParam("addressName", required = true) addressName: String): ResponseEntity<Any> {
        logger.debug("Deleting address with name $addressName for current user")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        userService.deleteDeliveryAddress(addressName, currUser)
        return ResponseEntity.ok("Address was successfully deleted")
    }

    @PreAuthorize(Constants.ANY_AUTHORIZED_AUTHORITY)
    @GetMapping("")
    fun getAllDeliveryAddresses(): ResponseEntity<Any> {
        logger.debug("Getting all delivery addresses for current user")

        val currUser = userService.getCurrentAuthorizedUser()
            ?: return ResponseEntity("Invalid authority", HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(userService.getAllDeliveryAddressesForms(currUser))
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DeliveryAddressController::class.java)
    }
}