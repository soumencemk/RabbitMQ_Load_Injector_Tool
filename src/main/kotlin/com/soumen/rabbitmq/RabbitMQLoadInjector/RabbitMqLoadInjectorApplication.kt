package com.soumen.rabbitmq.RabbitMQLoadInjector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class RabbitMqLoadInjectorApplication

fun main(args: Array<String>) {
	runApplication<RabbitMqLoadInjectorApplication>(*args)
}
