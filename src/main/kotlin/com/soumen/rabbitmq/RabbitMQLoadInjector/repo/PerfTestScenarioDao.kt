package com.soumen.rabbitmq.RabbitMQLoadInjector.repo

import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.PerfTestScenario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Repository
interface PerfTestScenarioDao : JpaRepository<PerfTestScenario, String>