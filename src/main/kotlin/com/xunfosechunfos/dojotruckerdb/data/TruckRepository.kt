package com.xunfosechunfos.dojotruckerdb.data

import com.xunfosechunfos.dojotruckerdb.common.parseToRows
import com.xunfosechunfos.dojotruckerdb.model.types.Point
import com.xunfosechunfos.dojotruckerdb.model.types.Truck
import org.springframework.stereotype.Repository
import org.springframework.util.ResourceUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

@Repository
class TruckRepository {
    private val repository = ConcurrentHashMap<String, Truck>()
    fun clearRepository() = repository.clear()

    fun put(truck: Truck) {
        repository[truck.id] = truck
    }

    fun list() = repository.elements().toList()
    fun getAsSequence() = repository.asSequence()

    init {
        loadFromFile()
    }

    private fun loadFromFile() {
        logger.info("Loading trucks from resource file")
        ResourceUtils.getFile("classpath:trucks.txt")
            .readText()
            .parseToRows()
            .map {
                val (id, model, cost, power, tankSize) = it.split("|")
                Truck(
                    id = id,
                    model = model,
                    cost = cost,
                    power = power,
                    tankSize = tankSize,
                    deliveryArea = listOf(
                        Point(latitude = 53.4169, longitude=9.7040),
                        Point(latitude = 54.4169, longitude=10.7040),
                        Point(latitude = 55.4169, longitude=11.7040),
                        Point(latitude = 53.4169, longitude=9.7040),
                    )
                )
            }
            .forEach { repository[it.id] = it }
        logger.info("${repository.size} Trucks loaded successfully")
    }

    companion object {
        private val logger = Logger.getLogger(TruckRepository::class.qualifiedName)
    }
}
