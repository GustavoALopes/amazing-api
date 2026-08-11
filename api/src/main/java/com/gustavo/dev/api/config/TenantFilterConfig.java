package com.gustavo.dev.api.config;

import com.gustavo.dev.tenant.TenantContextFilter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class TenantFilterConfig {

    @Bean
    public TenantContextFilter tenantContextFilter() {
        return new TenantContextFilter();
    }
}
