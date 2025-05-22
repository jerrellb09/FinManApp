package com.jay.home.finmanapp.service;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for structured logging with trace correlation.
 * Provides logging methods that automatically include trace and span IDs
 * for correlation in APM tools.
 */
@Service
public class LoggingService {
    private final Logger logger;
    
    /**
     * Constructor that uses the calling class name as the logger name.
     */
    public LoggingService() {
        // Get the caller class to use as the logger name
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerClassName = stackTrace[2].getClassName();
        this.logger = LoggerFactory.getLogger(callerClassName);
    }
    
    /**
     * Constructor that accepts a specific class for the logger name.
     * 
     * @param clazz the class to use for the logger name
     */
    public LoggingService(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Log an informational message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void info(String message, Object... args) {
        Span span = GlobalTracer.get().buildSpan("logging.info").start();
        try {
            setTraceAndSpanIds();
            logger.info(message, args);
        } finally {
            span.finish();
        }
    }
    
    /**
     * Log a warning message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void warn(String message, Object... args) {
        Span span = GlobalTracer.get().buildSpan("logging.warn").start();
        try {
            setTraceAndSpanIds();
            logger.warn(message, args);
        } finally {
            span.finish();
        }
    }
    
    /**
     * Log an error message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void error(String message, Object... args) {
        Span span = GlobalTracer.get().buildSpan("logging.error").start();
        try {
            setTraceAndSpanIds();
            logger.error(message, args);
        } finally {
            span.finish();
        }
    }
    
    /**
     * Log an error message with an exception and correlation IDs.
     * 
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void error(String message, Throwable throwable) {
        Span span = GlobalTracer.get().buildSpan("logging.error.exception").start();
        try {
            setTraceAndSpanIds();
            
            // Add error information to the current span
            span.setTag("error", true);
            
            Map<String, Object> errorLogs = new HashMap<>();
            errorLogs.put("event", "error");
            errorLogs.put("error.object", throwable);
            errorLogs.put("error.message", throwable.getMessage());
            errorLogs.put("error.kind", throwable.getClass().getName());
            
            span.log(errorLogs);
            
            logger.error(message, throwable);
        } finally {
            span.finish();
        }
    }
    
    /**
     * Log a debug message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void debug(String message, Object... args) {
        Span span = GlobalTracer.get().buildSpan("logging.debug").start();
        try {
            setTraceAndSpanIds();
            logger.debug(message, args);
        } finally {
            span.finish();
        }
    }
    
    /**
     * Sets the trace and span IDs from the active span in the MDC context.
     * This enables correlation between logs and traces.
     */
    private void setTraceAndSpanIds() {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.activeSpan();
        
        if (span != null && span.context() != null) {
            // Store generic trace IDs that will be picked up by any APM tool
            MDC.put("trace.id", span.context().toTraceId());
            MDC.put("span.id", span.context().toSpanId());
        }
    }
}