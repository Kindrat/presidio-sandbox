package com.github.kindrat.presidio

import com.github.kindrat.presidio.AnalysisServiceGrpc.AnalysisServiceImplBase
import com.github.kindrat.presidio.Builders.field
import com.github.kindrat.presidio.Builders.replacement
import com.github.kindrat.presidio.PresidioService.AnalysisReply
import com.github.kindrat.presidio.PresidioService.AnalysisRequest
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import types.Analyze.AnalyzeRequest
import types.Analyze.AnalyzeResponse
import types.AnalyzeServiceGrpc.AnalyzeServiceBlockingStub
import types.Anonymize.AnonymizeRequest
import types.AnonymizeServiceGrpc.AnonymizeServiceBlockingStub
import types.Template.AnalyzeTemplate
import types.Template.AnonymizeTemplate

private val logger = KotlinLogging.logger {}

class PresidioServiceImpl(
    private val analysisClient: AnalyzeServiceBlockingStub,
    private val anonymizeClient: AnonymizeServiceBlockingStub
) : AnalysisServiceImplBase() {

    override fun analyze(request: AnalysisRequest?, observer: StreamObserver<AnalysisReply>?) {
        val text = request?.text ?: throw IllegalArgumentException("Null request")
        Mono.fromCallable { analysisClient.apply(analyzeRequest(text)) }
            .map { anonymizeClient.apply(anonymizeRequest(text, it)) }
            .retry(10)
            .subscribeOn(Schedulers.elastic())
            .subscribe(
                {
                    logger.trace { "Handled request : ${it.text}" }
                    observer?.onNext(AnalysisReply.newBuilder().setText(it.text).build())
                    observer?.onCompleted()
                },
                {
                    logger.warn(it) { "Request failed" }
                    observer?.onError(it)
                }
            )
    }

    private fun analyzeRequest(text: String) =
        AnalyzeRequest.newBuilder()
            .setAnalyzeTemplate(
                AnalyzeTemplate.newBuilder()
                    .addFields(field("PHONE_NUMBER"))
                    .addFields(field("CREDIT_CARD"))
                    .build()
            )
            .setText(text)
            .build()

    private fun anonymizeRequest(text: String, analyzeResponse: AnalyzeResponse): AnonymizeRequest? {
        return AnonymizeRequest.newBuilder()
            .setText(text)
            .setTemplate(
                AnonymizeTemplate.newBuilder()
                    .addFieldTypeTransformations(replacement("PHONE_NUMBER", "<PHONE_NUMBER>"))
                    .addFieldTypeTransformations(replacement("CREDIT_CARD", "<CREDIT_CARD>"))
                    .build()
            )
            .addAllAnalyzeResults(analyzeResponse.analyzeResultsList)
            .build()
    }
}