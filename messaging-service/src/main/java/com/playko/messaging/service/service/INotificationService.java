package com.playko.messaging.service.service;

import com.playko.messaging.service.dto.SendNotification;

public interface INotificationService {
    void sendNotification(SendNotification sendNotification);
}