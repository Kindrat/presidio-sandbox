package com.github.kindrat.presidio

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

object ChannelFactory {
    fun newChannel(host: String, port: Int): ManagedChannel {
        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
    }
}