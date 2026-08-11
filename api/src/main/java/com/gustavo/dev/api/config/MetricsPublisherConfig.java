package com.gustavo.dev.api.config;

import com.gustavo.dev.observation.MicrometerMetricPublisher;
import com.gustavo.dev.observation.interfaces.IMetricsPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsPublisherConfig {

    @Bean
    public IMetricsPublisher micrometerPublisher(final MeterRegistry meterRegistry) {
        return new MicrometerMetricPublisher(meterRegistry);
    }
}
