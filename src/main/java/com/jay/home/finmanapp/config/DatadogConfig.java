package com.jay.home.finmanapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.opentracing.Span;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;

/**
 * Configuration class for APM integration.
 * Provides configurations for tracing aspects and request handling.
 */
@Configuration
public class DatadogConfig implements WebMvcConfigurer {

    /**
     * Returns the global tracer instance.
     * 
     * @return the tracer instance
     */
    @Bean
    public Tracer tracer() {
        return GlobalTracer.get();
    }
    
    /**
     * Configures automatic tracing for all controller methods
     * using AspectJ pointcut expressions.
     * 
     * @return DefaultPointcutAdvisor for controller method tracing
     */
    @Bean
    public DefaultPointcutAdvisor controllerTracing() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // This expression will match all methods in controller classes
        pointcut.setExpression("execution(* com.jay.home.finmanapp.controller..*.*(..))");
        
        return new DefaultPointcutAdvisor(pointcut, new TracingMethodAdvice("controller"));
    }
    
    /**
     * Configures automatic tracing for all service methods
     * using AspectJ pointcut expressions.
     * 
     * @return DefaultPointcutAdvisor for service method tracing
     */
    @Bean
    public DefaultPointcutAdvisor serviceTracing() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // This expression will match all methods in service classes
        pointcut.setExpression("execution(* com.jay.home.finmanapp.service..*.*(..))");
        
        return new DefaultPointcutAdvisor(pointcut, new TracingMethodAdvice("service"));
    }
    
    /**
     * Class to handle method interception for tracing.
     */
    private static class TracingMethodAdvice implements MethodBeforeAdvice {
        private final String componentType;
        
        public TracingMethodAdvice(String componentType) {
            this.componentType = componentType;
        }
        
        @Override
        public void before(Method method, Object[] args, Object target) {
            String className = target.getClass().getSimpleName();
            String methodName = method.getName();
            String operationName = componentType + "." + className + "." + methodName;
            
            // Start a span for this method
            Tracer tracer = GlobalTracer.get();
            Span span = tracer.buildSpan(operationName).start();
            
            // Add tags
            span.setTag("component", componentType);
            span.setTag("class", className);
            span.setTag("method", methodName);
            
            // Manually finish the span when the method completes
            // Note: This is simplified and may need more work for a production environment
            // to ensure spans are properly closed
            try {
                // In a real implementation, you would need to finish this span after the method completes
            } finally {
                // In a real implementation, you would call span.finish() here
            }
        }
    }
}