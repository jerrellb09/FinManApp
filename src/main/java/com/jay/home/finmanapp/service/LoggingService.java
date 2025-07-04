package com.jay.home.finmanapp.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * Service for structured logging with trace correlation.
 * Provides logging methods that automatically include trace and span IDs
 * for correlation in observability tools.
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
        setTraceCorrelation();
        logger.info(message, args);
        clearTraceCorrelation();
    }
    
    /**
     * Log a warning message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void warn(String message, Object... args) {
        setTraceCorrelation();
        logger.warn(message, args);
        clearTraceCorrelation();
    }
    
    /**
     * Log an error message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void error(String message, Object... args) {
        setTraceCorrelation();
        logger.error(message, args);
        clearTraceCorrelation();
    }
    
    /**
     * Log an error message with exception and correlation IDs.
     * 
     * @param message the message to log
     * @param throwable the exception to log
     * @param args arguments for the message format string
     */
    public void error(String message, Throwable throwable, Object... args) {
        setTraceCorrelation();
        logger.error(message, throwable, args);
        clearTraceCorrelation();
    }
    
    /**
     * Log a debug message with correlation IDs.
     * 
     * @param message the message to log
     * @param args arguments for the message format string
     */
    public void debug(String message, Object... args) {
        setTraceCorrelation();
        logger.debug(message, args);
        clearTraceCorrelation();
    }
    
    /**
     * Sets trace correlation information in MDC for structured logging.
     */
    private void setTraceCorrelation() {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            SpanContext spanContext = currentSpan.getSpanContext();
            if (spanContext.isValid()) {
                MDC.put("trace.id", spanContext.getTraceId());
                MDC.put("span.id", spanContext.getSpanId());
            }
        }
    }
    
    /**
     * Clears trace correlation information from MDC.
     */
    private void clearTraceCorrelation() {
        MDC.remove("trace.id");
        MDC.remove("span.id");
    }
}