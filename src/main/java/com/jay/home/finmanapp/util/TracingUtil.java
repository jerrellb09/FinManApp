package com.jay.home.finmanapp.util;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility class for manual tracing with OpenTelemetry.
 * Provides methods to create and manage spans for specific operations.
 */
@Component
public class TracingUtil {
    private static final Logger logger = LoggerFactory.getLogger(TracingUtil.class);
    
    private final Tracer tracer;

    @Autowired
    public TracingUtil(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Creates a new span for tracing an operation.
     *
     * @param operationName name of the operation being traced
     * @return the created span
     */
    public Span startSpan(String operationName) {
        return tracer.spanBuilder(operationName).startSpan();
    }

    /**
     * Creates a new span with additional attributes.
     *
     * @param operationName name of the operation being traced
     * @param attributes a map of key-value pairs to add as attributes to the span
     * @return the created span
     */
    public Span startSpan(String operationName, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName).startSpan();
        
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                span.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        
        return span;
    }

    /**
     * Records an exception in the current span.
     *
     * @param span the span to record the exception in
     * @param throwable the exception to record
     */
    public void recordException(Span span, Throwable throwable) {
        if (span != null) {
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
            logger.error("Error recorded in span: {}", throwable.getMessage(), throwable);
        }
    }

    /**
     * Executes a traced operation with automatic span management.
     *
     * @param operationName name of the operation
     * @param operation the operation to execute
     * @param <T> return type of the operation
     * @return result of the operation
     * @throws Exception if the operation throws an exception
     */
    public <T> T executeTraced(String operationName, TracedOperation<T> operation) throws Exception {
        Span span = startSpan(operationName);
        try (Scope scope = span.makeCurrent()) {
            T result = operation.execute();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            recordException(span, e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Functional interface for traced operations.
     */
    @FunctionalInterface
    public interface TracedOperation<T> {
        T execute() throws Exception;
    }
}