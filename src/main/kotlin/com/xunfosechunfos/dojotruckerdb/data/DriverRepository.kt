package com.xunfosechunfos.dojotruckerdb.data

import com.xunfosechunfos.dojotruckerdb.common.parseToRows
import com.xunfosechunfos.dojotruckerdb.model.types.Driver
import com.xunfosechunfos.dojotruckerdb.model.types.License
import com.xunfosechunfos.dojotruckerdb.model.types.LicenseType
import org.springframework.stereotype.Repository
import org.springframework.util.ResourceUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import kotlin.random.Random

@Repository
class DriverRepository {
    private val rng = Random(System.currentTimeMillis())
    private val repository = ConcurrentHashMap<String, Driver>()
    fun clearRepository() = repository.clear()

    fun put(item: Driver) {
        repository[item.id] = item
    }

    fun list() = repository.elements().toList()
    fun getAsSequence() = repository.asSequence()

    init {
        loadFromFile()
    }

    private fun loadFromFile() {
        logger.info("Loading drivers from resource file")
        ResourceUtils.getFile("classpath:drivers.txt")
            .readText()
            .parseToRows()
            .map {
                val (id, name) = it.split("|")
                val (firstName, lastName) = name.split(" ")
                Driver(
                    id = id,
                    firstName = firstName,
                    lastName = lastName,
                    license = randomLicense()
                )
            }
            .forEach { repository[it.id] = it }
        logger.info("${repository.size} Drivers loaded successfully")
    }


    private fun randomLicense(): License {
        val licenseType = LicenseType.values()[rng.nextInt(0, LicenseType.values().lastIndex)]
        return License(
            displayName = "License - ${licenseType.toString()}",
            type = licenseType
        )
    }

    companion object {
        private val logger = Logger.getLogger(DriverRepository::class.qualifiedName)
    }
}
