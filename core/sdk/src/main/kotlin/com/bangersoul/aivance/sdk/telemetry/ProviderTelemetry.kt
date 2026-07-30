package com.bangersoul.aivance.sdk.telemetry

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer

/**
 * Utility for recording telemetry data for AI Providers using OpenTelemetry.
 */
object ProviderTelemetry {
    private const val INSTRUMENTATION_NAME = "com.bangersoul.aivance.sdk"
    
    private val tracer: Tracer by lazy {
        GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME)
    }

    /**
     * Starts a new span for a provider request.
     *
     * @param providerId The ID of the provider making the request.
     * @param operation Name of the operation (e.g., "generateContent").
     * @return The started [Span].
     */
    fun startRequestSpan(providerId: String, operation: String): Span {
        return tracer.spanBuilder("$providerId.$operation")
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("provider.id", providerId)
            .setAttribute("operation.name", operation)
            .startSpan()
    }

    /**
     * Records a provider-specific error to a span and sets the span status.
     *
     * @param span The span to record the error on.
     * @param error The exception that occurred.
     * @param providerId The ID of the provider.
     */
    fun recordProviderError(span: Span, error: Throwable, providerId: String) {
        span.recordException(error, Attributes.of(
            AttributeKey.stringKey("provider.id"), providerId
        ))
        span.setStatus(StatusCode.ERROR, error.message ?: "Provider error")
    }

    /**
     * Records a generic event for a provider.
     *
     * @param providerId The ID of the provider.
     * @param eventName Name of the event.
     * @param attributes Additional attributes for the event.
     */
    fun recordEvent(providerId: String, eventName: String, attributes: Attributes = Attributes.empty()) {
        val currentSpan = Span.current()
        if (currentSpan != Span.getInvalid()) {
            val eventAttributes = Attributes.builder()
                .putAll(attributes)
                .put("provider.id", providerId)
                .build()
            currentSpan.addEvent(eventName, eventAttributes)
        }
    }
}
