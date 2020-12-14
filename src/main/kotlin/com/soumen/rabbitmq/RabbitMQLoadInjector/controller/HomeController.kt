package com.soumen.rabbitmq.RabbitMQLoadInjector.controller

import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.PerfTestScenario
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.TaskStatus
import com.soumen.rabbitmq.RabbitMQLoadInjector.entity.TaskStatus.NOT_STARTED
import com.soumen.rabbitmq.RabbitMQLoadInjector.repo.PerfTestScenarioDao
import com.soumen.rabbitmq.RabbitMQLoadInjector.services.LoadInjector
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import java.util.function.Consumer

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Controller
class HomeController(val perfTestScenarioDao: PerfTestScenarioDao, val injector: LoadInjector) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/")
    fun home(model: Model): String? {
        return homeRedirect(model)
    }

    @GetMapping(value = ["/startAll"])
    fun handleStartAll(model: Model): String? {
        val errorMessages = StringBuilder()
        val successMessages = StringBuilder()
        perfTestScenarioDao.findAll().also {
            it.forEach(Consumer {
                try {
                    this.startTest(it.testName!!)
                    successMessages.append(it.testName).append(" STARTED...   ")
                } catch (e: Exception) {
                    errorMessages.append(e.message)
                    errorMessages.append("\n")
                }
            })
        }
        if (errorMessages.isNotEmpty()) {
            model.addAttribute("errorMsg", errorMessages.toString())
        }
        if (successMessages.isNotEmpty()) {
            model.addAttribute("successMsg", successMessages.toString())
        }
        return homeRedirect(model)
    }

    @GetMapping(value = ["/stopAll"])
    fun handleStopAll(model: Model): String? {
        val errorMessages = StringBuilder()
        val successMessages = StringBuilder()
        perfTestScenarioDao.findAll().also {
            it.forEach(Consumer {
                try {
                    this.stopTest(it.testName)
                    successMessages.append(it.testName).append(" STOPPED...   ")
                } catch (e: Exception) {
                    errorMessages.append(e.message)
                    errorMessages.append("\n")
                }
            })
        }
        if (errorMessages.isNotEmpty()) {
            model.addAttribute("errorMsg", errorMessages.toString())
        }
        if (successMessages.isNotEmpty()) {
            model.addAttribute("successMsg", successMessages.toString())
        }
        return homeRedirect(model)
    }


    @GetMapping(value = ["/delete_Test"])
    fun handleDeleteUser(@RequestParam(name = "testName") testName: String, model: Model): String? {
        perfTestScenarioDao.deleteById(testName)
        model.addAttribute("successMsg", "DELETED - $testName")
        return homeRedirect(model)
    }

    @PostMapping("/submitTask")
    fun submitTask(@ModelAttribute perfTestScenario: PerfTestScenario, model: Model): String? {
        try {
            logger.info("Creating task...")
            if (!perfTestScenarioDao.existsById(perfTestScenario.testName!!)) {
                perfTestScenario.scheduleTime = Date()
                perfTestScenario.status = NOT_STARTED
                perfTestScenarioDao.save(perfTestScenario)
            } else {
                model.addAttribute("errorMsg", "Task name : ${perfTestScenario.testName} already exists !")
            }
        } catch (e: Exception) {
            logger.error { e }
            model.addAttribute("errorMsg", e.message)
        }
        return homeRedirect(model)
    }

    private fun homeRedirect(model: Model): String {
        model.addAttribute("perfTest", PerfTestScenario())
        model.addAttribute("perfTestsList", perfTestScenarioDao.findAll())
        return "home"
    }


    @GetMapping("/startTest")
    fun startTest(@RequestParam("testName") testName: String, model: Model): String? {
        try {
            this.startTest(testName)
            model.addAttribute("successMsg", "STARTED - $testName")
        } catch (e: Exception) {
            logger.error { e }
            model.addAttribute("errorMsg", e.message)
        }
        return homeRedirect(model)
    }

    private fun startTest(testName: String) {
        perfTestScenarioDao.findById(testName).get().also {
            injector.run {
                if (!isAlreadyRunning(testName)) {
                    logger.info("Starting ... $testName")
                    startPerfTest(it)
                    changeTaskStatus(testName, TaskStatus.IN_PROGRESS)
                } else {
                    throw RuntimeException("$testName already in Progress")
                }
            }
        }
    }


    private fun stopTest(testName: String?) {
        injector.apply {
            stopTest(testName!!)
            changeTaskStatus(testName, TaskStatus.USER_STOPPED)
            updateStartTime(testName, null)
        }
    }


    @GetMapping("/stopTest")
    fun stopTest(@RequestParam("testName") testName: String, model: Model): String? {
        logger.info("stopping ... $testName")
        try {
            stopTest(testName)
            model.addAttribute("successMsg", "STOPPED - $testName")
        } catch (e: Exception) {
            model.addAttribute("errorMsg", e.message)
        }
        return homeRedirect(model)
    }
}