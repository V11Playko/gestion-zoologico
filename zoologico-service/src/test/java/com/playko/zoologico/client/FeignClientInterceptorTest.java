package com.playko.zoologico.client;

import com.playko.zoologico.client.interceptor.FeignClientInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class FeignClientInterceptorTest {

    private FeignClientInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FeignClientInterceptor();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void apply_shouldForwardAuthorizationHeader_whenRequestAttributesPresent() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer token-de-prueba");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get("Authorization"))
                .isNotNull()
                .contains("Bearer token-de-prueba");
    }

    @Test
    void apply_shouldSetEmptyAuthorizationHeader_whenNoRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        // No debe lanzar excepción y el header queda vacío o presente pero vacío
        assertThat(template.headers()).containsKey("Authorization");
    }
}
