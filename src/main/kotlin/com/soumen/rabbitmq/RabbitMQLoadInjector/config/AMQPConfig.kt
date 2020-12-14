package com.soumen.rabbitmq.RabbitMQLoadInjector.config

import com.rabbitmq.client.ConnectionFactory
import mu.KotlinLogging
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * @author Soumen Karmakar
 * @Date 08/12/2020
 */
@Configuration
class AMQPConfig {

    @Value("\${usessl}")
    private val usessl = false

    @Value("\${keystore.file}")
    private val keyStoreFile: String? = null

    @Value("\${truststore.file}")
    private val trustoreFile: String? = null

    @Value("\${keystore.pass}")
    private val keyStorePassword: String? = null

    @Value("\${trustore.pass}")
    private val trustStorePassword: String? = null

    @Value("\${rabbitmq.user}")
    private val rabbitUser: String? = null

    @Value("\${rabbitmq.pass}")
    private val rabbitPassword: String? = null

    @Value("\${rabbitmq.hosts}")
    private val hosts: String? = null

    @Value("\${rabbitmq.port}")
    private val rabbitPort: String? = null

    private val logger = KotlinLogging.logger {}

    @Bean(name = ["rabbitConnectionFactory"])
    fun connectionFactory(): CachingConnectionFactory? {
        val sslContext: SSLContext by lazy { SSLContext.getInstance("TLSv1.2") }
        if (usessl) {
            try {
                val ks = KeyStore.getInstance("JKS", "SUN")
                ks.load(FileInputStream(keyStoreFile), keyStorePassword!!.toCharArray())
                val kmf = KeyManagerFactory.getInstance("SunX509")
                kmf.init(ks, keyStorePassword.toCharArray())
                val tks = KeyStore.getInstance("JKS", "SUN")
                tks.load(FileInputStream(trustoreFile), trustStorePassword!!.toCharArray())
                val tmf = TrustManagerFactory.getInstance("SunX509")
                tmf.init(tks)
                sslContext.init(kmf.keyManagers, tmf.trustManagers, null)
            } catch (e: Exception) {
                logger.error { e }
            }
        }
        var firstHost = hosts
        if ("," in hosts!!) {
            firstHost = hosts.split(",")[0].trim()
        }
        val factory = ConnectionFactory().apply {
            host = firstHost
            port = rabbitPort!!.toInt()
            username = rabbitUser
            password = rabbitPassword
            if (usessl) useSslProtocol(sslContext)
        }
        val connectionFactory = CachingConnectionFactory(factory).apply {
            setAddresses(hosts)
            username = rabbitUser!!
            setPassword(rabbitPassword!!)
        }
        logger.info { "Successfully initialised AMQP connection" }
        return connectionFactory
    }

    @Bean
    fun getRabbitTemplate(connectionFactory: CachingConnectionFactory?) = RabbitTemplate(connectionFactory!!)
}