package com.soumen.rabbitmq.RabbitMQLoadInjector.services

import com.google.common.util.concurrent.RateLimiter
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.PerfTestScenario
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.Step
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.TaskStatus
import com.soumen.rabbitmq.RabbitMQLoadInjector.repo.PerfTestScenarioDao
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.lang.System.currentTimeMillis
import java.time.Duration
import java.util.*

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Service
class LoadInjector(val amqpPublisher: AMQPPublisher, val perfTestScenarioDao: PerfTestScenarioDao) {

    private val logger = KotlinLogging.logger {}

    companion object {
        private val RUNNINGTASKS: MutableSet<String> by lazy { Collections.synchronizedSet(HashSet()) }
        private val STOPREQUESTS: MutableSet<String> by lazy { Collections.synchronizedSet(HashSet()) }
    }

    @Async
    fun startPerfTest(scenario: PerfTestScenario) {
        RUNNINGTASKS += scenario.testName!!
        this.updateStartTime(scenario.testName!!, Date())
        val steps: List<Step> = fetchSteps(scenario.tpss!!.split(","), scenario.durations!!.split(","))
        for (i in steps.indices) {
            val step: Step = steps[i]
            logger.info("STEP --  ${step.tps}  DURATION -  ${step.duration}")
            val endTime: Long = currentTimeMillis() + step.duration * 1000
            val rateLimiter: RateLimiter = RateLimiter.create(step.tps.toDouble())
            while (currentTimeMillis() < endTime) {
                if (STOPREQUESTS.isNotEmpty() && scenario.testName in STOPREQUESTS) {
                    logger.info("Interrupted .. ${scenario.testName}")
                    this.changeTaskStatus(scenario.testName!!, TaskStatus.USER_STOPPED)
                    STOPREQUESTS.remove(scenario.testName)
                    RUNNINGTASKS.remove(scenario.testName)
                    return
                }
                if (currentTimeMillis() >= endTime) {
                    break
                }
                if (rateLimiter.tryAcquire(1, Duration.ZERO)) {
                    amqpPublisher.publish(scenario.exchangeName, scenario.routingKey, scenario.payload)
                }
            }
        }
        this.changeTaskStatus(scenario.testName!!, TaskStatus.FINISHED)
        RUNNINGTASKS.remove(scenario.testName)
    }

    fun stopTest(testName: String) = if (testName in RUNNINGTASKS) {
        STOPREQUESTS += testName
    } else {
        throw RuntimeException("$testName Not running at the moment!")
    }

    fun updateStartTime(testName: String, startTime: Date?) {
        logger.info { "Start time - $testName :: $startTime" }
        perfTestScenarioDao.findById(testName).get().run {
            actualStartTime = startTime
            perfTestScenarioDao.save(this)
        }
    }

    fun changeTaskStatus(testName: String, status: TaskStatus) {
        logger.info { "TEST : $testName STATUS : $status" }
        perfTestScenarioDao.findById(testName).get().run {
            this.status = status
            perfTestScenarioDao.save(this)
        }
    }

    fun isAlreadyRunning(taskName: String) = taskName in RUNNINGTASKS

    private fun fetchSteps(tpss: List<String>, durations: List<String>): List<Step> {
        val list = ArrayList<Step>()
        if (durations.size > 1 && tpss.size != durations.size) {
            throw RuntimeException("TPS and DURATIONS count mismatch")
        } else {
            for (i in tpss.indices) {
                val step = if (durations.size == 1) {
                    Step(tpss[i].toInt(), durations[0].toInt())
                } else {
                    Step(tpss[i].toInt(), durations[i].toInt())
                }
                list.add(step)
            }
        }
        return list
    }
}