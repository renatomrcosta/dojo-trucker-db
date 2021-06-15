package com.xunfosechunfos.dojotruckerdb

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.client.DefaultGraphQLClient
import com.netflix.graphql.dgs.client.HttpResponse
import com.netflix.graphql.dgs.client.RequestExecutor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.collections.HashMap

@Configuration
class ConfigTests {
    @Bean
    fun restTemplate() = RestTemplate()
}

@SpringBootTest
class TestQuery {

    @Autowired
    private lateinit var queryExecutor: DgsQueryExecutor

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Test
    fun shouldCallUsingHTTP() {
        // This test only passes when your local is running the service. This is meant as a code sample for Hoppers
        val client = DefaultGraphQLClient("http://localhost:8080/graphql")

        val response = client.executeQuery("""
            {
                trucks {
                    id
                    model
                    cost
                    power
                }
            }
        """.trimIndent(), emptyMap()) { url, headers, body ->
            // This configuration is necessary because the DGS client is client agnostic.
            val requestHeaders = HttpHeaders()
            headers.forEach { requestHeaders[it.key] = it.value }

            val exchange =
                restTemplate.exchange(url, HttpMethod.POST, HttpEntity(body, requestHeaders), String::class.java)

            HttpResponse(exchange.statusCodeValue, exchange.body)
        }

        println(response)
        val wrapper = response.dataAsObject(MyTruckWrapper::class.java)
        wrapper.trucks.forEach { println(it) }
    }

    @Test
    fun shouldFetchAllTrucksUsingExecute() {
        val result = queryExecutor.execute("""
                {
                    trucks {
                        id
                        model
                        cost
                        power
                    }
                }
            """.trimIndent())


        // This does not work properly, giving back a list of LinkedHashMap instead... I might be missing some config for serialization, but this seems like a classic type erasure shenanigan
//        val trucks = result.getData<List<MyTruck>>()
        // Therefore, I manually deserialize the list:
        val data = result.getData<HashMap<String, List<HashMap<String, String>
                >>>()
        val trucks = data["trucks"]?.map { it.toMyTruck() } ?: error("no trucks found")
        Assertions.assertEquals(100, trucks.size)
        trucks.forEach { println(it) }
    }

    @Test
    fun shouldFetchAllTrucks() {
        val result: List<MyTruck> = queryExecutor.executeAndExtractJsonPathAsObject("""
                {
                    trucks {
                        id
                        model
                        cost
                        power
                    }
                }
            """.trimIndent(), "data.trucks", List::class.java) as List<MyTruck> //ugly af


        println("Trucks: $result")
        Assertions.assertEquals(100, result.size)
        result.forEach(::println)
    }

    @Test
    fun shouldGetSpecificTruck() {
        val truck = queryExecutor.executeAndExtractJsonPathAsObject("""
            {
                trucks(id:"f37e181e-cb32-4674-b8da-aece7e34da25") {
                    id
                    model
                    cost
                    power
                }
            }
        """.trimIndent(), "data.trucks[0]", MyTruck::class.java)

        Assertions.assertEquals(MyTruck(
            id = "f37e181e-cb32-4674-b8da-aece7e34da25",
            model = "Volvo FH16",
            cost = "€101,755",
            power = "540 HP",
        ), truck)
    }
}

private fun HashMap<String, String>.toMyTruck(): MyTruck = MyTruck(
    id = this["id"] ?: error("no id"),
    model = this["model"] ?: error("no model"),
    cost = this["cost"] ?: error("no cost"),
    power = this["power"] ?: error("no power"),
)

data class MyTruck(val id: String, val model: String, val cost: String, val power: String)

data class MyTruckWrapper(val trucks: List<MyTruck>)
