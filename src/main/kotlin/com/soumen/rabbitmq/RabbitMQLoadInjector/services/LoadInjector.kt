package com.soumen.rabbitmq.RabbitMQLoadInjector.services

import com.google.common.util.concurrent.RateLimiter
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.PerfTestScenario
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.Step
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.TaskStatus
import com.soumen.rabbitmq.RabbitMQLoadInjector.repo.PerfTestScenarioDao
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Service
class LoadInjector(val amqpPublisher: AMQPPublisher, val perfTestScenarioDao: PerfTestScenarioDao) {

    private val RUNNINGTASKS = Collections.synchronizedSet(HashSet<String>())
    private val STOPREQUESTS = Collections.synchronizedSet(HashSet<String>())

    companion object : KLogging()

    @Async
    fun startPerfTest(scenario: PerfTestScenario) {
        RUNNINGTASKS.add(scenario.testName)
        updateStartTime(scenario.testName!!, Date())
        val steps: List<Step> = fetchSteps(scenario.tpss!!.split(","), scenario.durations!!.split(","))
        for (i in steps.indices) {
            val step: Step = steps[i]
            logger.info("STEP -- " + step.tps + " DURATION - " + step.duration)
            val endTime: Long = System.currentTimeMillis() + step.duration * 1000
            val rateLimiter: RateLimiter = RateLimiter.create(step.tps.toDouble())
            while (true) {
                if (STOPREQUESTS.isNotEmpty() && STOPREQUESTS.contains(scenario.testName)) {
                    logger.info("Interrupted .. " + scenario.testName)
                    changeTaskStatus(scenario.testName, TaskStatus.USER_STOPPED)
                    STOPREQUESTS.remove(scenario.testName)
                    RUNNINGTASKS.remove(scenario.testName)
                    return
                }
                if (System.currentTimeMillis() >= endTime) {
                    break
                }
                val acquired: Boolean = rateLimiter.tryAcquire(1, Duration.ZERO)
                if (acquired) {
                    amqpPublisher.publish(scenario.exchangeName, scenario.routingKey, scenario.payload)
                }
            }
        }
        changeTaskStatus(scenario.testName, TaskStatus.FINISHED)
        RUNNINGTASKS.remove(scenario.testName)
    }

    fun stopTest(testName: String) {
        if (RUNNINGTASKS.contains(testName)) {
            STOPREQUESTS.add(testName)
        } else {
            throw RuntimeException("$testName Not running at the moment!")
        }
    }

    fun updateStartTime(testName: String, startTime: Date?) {
        val perfTestScenario: PerfTestScenario = perfTestScenarioDao.findById(testName).get()
        perfTestScenario.actualStartTime = startTime
        perfTestScenarioDao.save(perfTestScenario)
    }

    fun changeTaskStatus(testName: String?, status: TaskStatus?) {
        val perfTestScenario: PerfTestScenario = perfTestScenarioDao.findById(testName!!).get()
        perfTestScenario.status = status
        perfTestScenarioDao.save(perfTestScenario)
    }

    fun isAlreadyRunning(taskName: String?): Boolean {
        return RUNNINGTASKS.contains(taskName)
    }

    private fun fetchSteps(tpss: List<String>, durations: List<String>): List<Step> {
        val list: MutableList<Step> = ArrayList<Step>()
        if (durations.size > 1 && tpss.size != durations.size) {
            throw RuntimeException("TPS and DuRATIONS count mismatch")
        } else {
            var step: Step
            for (i in tpss.indices) {
                step = if (durations.size == 1) {
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