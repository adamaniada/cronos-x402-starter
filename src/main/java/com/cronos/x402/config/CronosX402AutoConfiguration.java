package com.cronos.x402.config;

import com.cronos.x402.aspect.PaymentGuardAspect;
import com.cronos.x402.exception.X402ExceptionHandler;
import com.cronos.x402.service.CronosService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CronosProperties.class)
public class CronosX402AutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "cronos.x402", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CronosService cronosService(CronosProperties properties) {
        return new CronosService(properties.getRpcUrl());
    }

    @Bean
    public PaymentGuardAspect paymentGuardAspect(CronosService cronosService, CronosProperties properties) {
        return new PaymentGuardAspect(cronosService, properties);
    }
    
    @Bean
    public X402ExceptionHandler x402ExceptionHandler() {
        return new X402ExceptionHandler();
    }
}
