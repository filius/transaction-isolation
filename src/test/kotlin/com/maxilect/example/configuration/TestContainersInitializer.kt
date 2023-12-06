package com.maxilect.example.configuration

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait

class TestContainersInitializer  : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val dockerComposeFile = applicationContext.getResource("docker/docker-compose.yml").file
        val dockerComposeContainer = DockerComposeContainer<Nothing>(dockerComposeFile)
            .withExposedService("mysql", 1, 3306)
            .waitingFor("mysql", Wait.forListeningPort())
        dockerComposeContainer.start()

        val mysqlHost = dockerComposeContainer.getServiceHost("mysql_1", 3306)
        val mysqlPort = dockerComposeContainer.getServicePort("mysql_1", 3306)

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "spring.datasource.url=jdbc:mysql://$mysqlHost:$mysqlPort/test"
        )
    }
}