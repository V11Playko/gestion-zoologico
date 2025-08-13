package com.playko.zoologico.client;

import com.playko.zoologico.client.dto.SendNotification;
import com.playko.zoologico.client.interceptor.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "messaging-service",
        url = "http://localhost:8092/api/notifications",
        configuration = FeignClientInterceptor.class
)
public interface MessagingClient {

    @PostMapping("/send")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO','ROLE_CLIENTE')")
    void sendNotification(@RequestBody SendNotification sendNotification);
}
