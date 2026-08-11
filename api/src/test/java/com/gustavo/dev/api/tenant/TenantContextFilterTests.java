package com.gustavo.dev.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import com.gustavo.dev.tenant.TenantContext;

import com.gustavo.dev.tenant.TenantContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantContextFilterTests {

    private final TenantContextFilter filter = new TenantContextFilter();

    @Test
    void exposesTenantIdWhileProcessingRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TenantContextFilter.TENANT_ID_HEADER, "tenant-123");
        AtomicReference<String> capturedTenantId = new AtomicReference<>();

        filter.doFilter(
            request,
            new MockHttpServletResponse(),
            (ignoredRequest, ignoredResponse) -> capturedTenantId.set(TenantContext.getTenantId())
        );

        assertThat(capturedTenantId.get()).isEqualTo("tenant-123");
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void leavesContextUnboundWhenHeaderIsMissing() throws Exception {
        AtomicReference<String> capturedTenantId = new AtomicReference<>("not-called");

        filter.doFilter(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            (ignoredRequest, ignoredResponse) -> capturedTenantId.set(TenantContext.getTenantId())
        );

        assertThat(capturedTenantId.get()).isNull();
        assertThat(TenantContext.getTenantId()).isNull();
    }
}
