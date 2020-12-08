package com.soumen.rabbitmq.RabbitMQLoadInjector.entity

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Entity
data class PerfTestScenario (
    @Id
    var testName: String?,
    var tpss: String?,
    var durations: String?,
    var exchangeName: String?,
    var routingKey: String?,
    var status: TaskStatus?,
    var payload: String?,
    var scheduleTime: Date?,
    var actualStartTime: Date?,
) {
    constructor() : this(null,null,null,null,null,null,null,null,null)
}