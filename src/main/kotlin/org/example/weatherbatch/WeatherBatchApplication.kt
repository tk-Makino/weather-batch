package org.example.weatherbatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WeatherBatchApplication

fun main(args: Array<String>) {
    runApplication<WeatherBatchApplication>(*args)
}
