package com.jay.home.finmanapp.util;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for manual tracing with OpenTracing.
 * Provides methods to create and manage spans for specific operations.
 */
public class TracingUtil {
    private static final Logger logger = LoggerFactory.getLogger(TracingUtil.class);

    /**
     * Creates a new span as a child of the current active span.
     *
     * @param operationName name of the operation being traced
     * @return the created span
     */
    public static Span startSpan(String operationName) {
        Tracer tracer = GlobalTracer.get();
        return tracer.buildSpan(operationName).start();
    }

    /**
     * Creates a new span with additional tags.
     *
     * @param operationName name of the operation being traced
     * @param tags a map of key-value pairs to add as tags to the span
     * @return the created span
     */
    public static Span startSpan(String operationName, Map<String, Object> tags) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan(operationName).start();
        
        if (tags != null) {
            for (Map.Entry<String, Object> entry : tags.entrySet()) {
                span.setTag(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        return span;
    }

    /**
     * Adds an error to the current active span.
     *
     * @param throwable the exception to record
     */
    public static void recordException(Throwable throwable) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.activeSpan();
        
        if (span != null) {
            span.setTag("error", true);
            
            Map<String, Object> errorLogs = new HashMap<>();
            errorLogs.put("event", "error");
            errorLogs.put("error.object", throwable);
            errorLogs.put("error.message", throwable.getMessage());
            errorLogs.put("error.kind", throwable.getClass().getName());
            errorLogs.put("stack", getStackTraceAsString(throwable));
            
            span.log(errorLogs);
            logger.error("Error recorded in span: {}", throwable.getMessage(), throwable);
        }
    }

    /**
     * A traced method example.
     * Add your own custom spans around important operations.
     */
    public static void tracedMethod() {
        Span span = startSpan("custom.operation");
        try {
            // Method logic here will be traced
            logger.info("Executing traced method");
        } finally {
            span.finish();
        }
    }

    /**
     * Converts a throwable's stack trace to a string.
     *
     * @param throwable the exception to convert
     * @return the stack trace as a string
     */
    private static String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        return sb.toString();
    }
}