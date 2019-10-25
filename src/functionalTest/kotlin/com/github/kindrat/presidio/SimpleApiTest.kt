package com.github.kindrat.presidio

import com.github.kindrat.presidio.AnalysisServiceGrpc.newBlockingStub
import com.github.kindrat.presidio.ChannelFactory.newChannel
import com.github.kindrat.presidio.PresidioService.AnalysisRequest
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec

class SimpleApiTest: BehaviorSpec() {
    init {
        Given("Text with phone and credit card") {
            val requestPayload = "my phone number is 057-555-2323 and my credit card is 4961-2765-5327-5913"
            val expectedResponsePayload = "my phone number is <PHONE_NUMBER> and my credit card is <CREDIT_CARD>"
            val client = newBlockingStub(newChannel("localhost", 8080))

            When("Making gRPC call with sensitive payload") {
                val request = AnalysisRequest.newBuilder().setText(requestPayload).build()
                val response = client.analyze(request)

                Then("Response does not contain sensitive payload") {
                    response.text shouldBe expectedResponsePayload
                }
            }
        }
    }
}