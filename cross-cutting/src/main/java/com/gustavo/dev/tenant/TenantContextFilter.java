package com.gustavo.dev.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public final class TenantContextFilter extends OncePerRequestFilter {

    public static final String TENANT_ID_HEADER = "tenantId";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_ID_HEADER);

        if (tenantId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TenantContext.runWithTenantId(
                tenantId,
                () -> filterChain.doFilter(request, response)
            );
        } catch (ServletException | IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServletException("Failed to process request tenant context", exception);
        }
    }
}
