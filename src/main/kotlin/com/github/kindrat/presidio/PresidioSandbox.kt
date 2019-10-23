package com.github.kindrat.presidio

import io.grpc.ServerBuilder
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    val serverPort = System.getenv()["APP_SERVER_PORT"]?.toInt() ?: 9000
    val eventServer = ServerBuilder.forPort(serverPort)
        .addService(PresidioServiceImpl())
        .build()
    eventServer.start()
    logger.info { "PresidioSandbox Server is started on port $serverPort" }

    Runtime.getRuntime().addShutdownHook(Thread { eventServer.shutdown() })
    eventServer.awaitTermination()
}