package pl.edu.uj.tcs.aiplayground.dto;

import java.util.UUID;

public record AlertEvent(
        String message,
        Boolean isInfo,
        UUID id) {
    public static AlertEvent createAlertEvent(String message, Boolean isInfo) {
        return new AlertEvent(message, isInfo, UUID.randomUUID());
    }
}
