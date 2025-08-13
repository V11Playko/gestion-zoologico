package com.playko.zoologico.client.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Component
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        final String authorization = HttpHeaders.AUTHORIZATION;
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            String authorizationHeader = requestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            template.header(authorization, authorizationHeader);
        } else {
            // Logueamos un aviso indicando el archivo/clase donde ocurre
            log.warn("En el archivo FeignClientInterceptor.apply: RequestAttributes is null in FeignClientInterceptor.java");
            template.header(authorization, "");
        }
    }
}
