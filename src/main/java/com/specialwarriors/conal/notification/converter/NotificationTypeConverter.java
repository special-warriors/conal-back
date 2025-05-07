package com.specialwarriors.conal.notification.converter;

import com.specialwarriors.conal.notification.enums.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public NotificationType convertToEntityAttribute(String type) {

        return NotificationType.valueOf(type);
    }

    @Override
    public String convertToDatabaseColumn(NotificationType notificationType) {

        return notificationType.name();
    }
}
