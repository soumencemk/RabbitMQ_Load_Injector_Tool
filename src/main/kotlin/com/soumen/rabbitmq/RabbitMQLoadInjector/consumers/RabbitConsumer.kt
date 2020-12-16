package com.soumen.rabbitmq.RabbitMQLoadInjector.consumers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.function.Consumer

/**
 * @author Soumen Karmakar
 * @Date 16/12/2020
 */
@Component
class RabbitConsumer : Consumer<Any> {
    @Value("\${consumer.isLogReqd:false}")
    private val isLogReqd: Boolean? = null
    private val logger = KotlinLogging.logger {}


    override fun accept(t: Any) {
        if (isLogReqd!!) {
            logger.info { "Received : $t" }
        }
    }

}