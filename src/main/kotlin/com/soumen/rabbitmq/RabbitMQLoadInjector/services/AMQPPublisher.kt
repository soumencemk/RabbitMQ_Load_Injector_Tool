package com.soumen.rabbitmq.RabbitMQLoadInjector.services

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Service
class AMQPPublisher(val rabbitTemplate: RabbitTemplate) {
    fun publish(exchange: String?, routingKey: String?, message: String?) = rabbitTemplate.convertAndSend(exchange!!, routingKey!!, message!!)
}