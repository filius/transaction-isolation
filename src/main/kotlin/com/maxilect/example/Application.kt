package com.maxilect.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.maxilect"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}