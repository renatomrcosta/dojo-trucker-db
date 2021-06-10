package com.xunfosechunfos.dojotruckerdb.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.xunfosechunfos.dojotruckerdb.data.DriverRepository
import com.xunfosechunfos.dojotruckerdb.data.TruckRepository
import com.xunfosechunfos.dojotruckerdb.model.types.Driver
import com.xunfosechunfos.dojotruckerdb.model.types.Truck
import java.util.*

@DgsComponent
class QueryHandler(
    private val truckRepository: TruckRepository,
    private val driverRepository: DriverRepository,
) {

    @DgsQuery
    fun trucks(@InputArgument id: String?): List<Truck> {
        return truckRepository.list().filter { id == null || it.id == id }
    }

    @DgsQuery
    fun drivers(@InputArgument id: String?): List<Driver> {
        return driverRepository.list().filter { id == null || it.id == id }
    }
}
