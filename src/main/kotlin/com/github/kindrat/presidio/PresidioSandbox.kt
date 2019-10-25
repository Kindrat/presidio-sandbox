package com.github.kindrat.presidio

import com.github.kindrat.presidio.ChannelFactory.newChannel
import io.grpc.ServerBuilder
import mu.KotlinLogging
import types.AnalyzeServiceGrpc
import types.AnonymizeServiceGrpc

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    val serverPort = System.getenv()["APP_SERVER_PORT"]?.toInt() ?: 9000

    val analyzerHost = System.getenv()["ANALYZER_HOST"] ?: "localhost"
    val analyzerPort = System.getenv()["ANALYZER_PORT"]?.toInt() ?: 3000

    val anonymizerHost = System.getenv()["ANONYMIZER_HOST"] ?: "localhost"
    val anonymizerPort = System.getenv()["ANONYMIZER_PORT"]?.toInt() ?: 3001

    val analysisClient = AnalyzeServiceGrpc.newBlockingStub(newChannel(analyzerHost, analyzerPort))
    val client = AnonymizeServiceGrpc.newBlockingStub(newChannel(anonymizerHost, anonymizerPort))
    val eventServer = ServerBuilder.forPort(serverPort)
        .addService(PresidioServiceImpl(analysisClient, client))
        .build()
    eventServer.start()
    logger.info { "PresidioSandbox Server is started on port $serverPort" }

    Runtime.getRuntime().addShutdownHook(Thread { eventServer.shutdown() })
    eventServer.awaitTermination()
}