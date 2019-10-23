package com.github.kindrat.presidio

import com.github.kindrat.presidio.AnalysisServiceGrpc.AnalysisServiceImplBase
import com.github.kindrat.presidio.PresidioService.AnalysisReply
import com.github.kindrat.presidio.PresidioService.AnalysisRequest
import io.grpc.stub.StreamObserver

class PresidioServiceImpl : AnalysisServiceImplBase() {
    override fun analyze(request: AnalysisRequest?, responseObserver: StreamObserver<AnalysisReply>?) {
        super.analyze(request, responseObserver)
    }
}