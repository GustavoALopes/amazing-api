package com.gustavo.dev.api.config;

import com.gustavo.dev.observation.MicrometerMetricPublisher;
import com.gustavo.dev.observation.interfaces.IMetricsPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetrictsPublisherConfig {

    @Bean
    public IMetricsPublisher micrometerPublisher() {
        return new MicrometerMetricPublisher();
    }
}
