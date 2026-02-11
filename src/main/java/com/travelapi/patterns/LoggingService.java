package com.travelapi.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void info(String message) {
        String formattedMessage = formatMessage("INFO", message);
        logger.info(formattedMessage);
    }

    public void info(String className, String methodName, String message) {
        String formattedMessage = formatMessage("INFO", className, methodName, message);
        logger.info(formattedMessage);
    }

    public void error(String message, Throwable throwable) {
        String formattedMessage = formatMessage("ERROR", message);
        logger.error(formattedMessage, throwable);
    }

    public void error(String className, String methodName, String message, Throwable throwable) {
        String formattedMessage = formatMessage("ERROR", className, methodName, message);
        logger.error(formattedMessage, throwable);
    }

    public void warn(String message) {
        String formattedMessage = formatMessage("WARN", message);
        logger.warn(formattedMessage);
    }

    public void debug(String message) {
        String formattedMessage = formatMessage("DEBUG", message);
        logger.debug(formattedMessage);
    }

    public void logApiRequest(String endpoint, String method, String clientIp) {
        String message = String.format("API Request - Method: %s, Endpoint: %s, Client: %s", method, endpoint, clientIp);
        info(message);
    }

    public void logApiResponse(String endpoint, int statusCode, long executionTime) {
        String message = String.format("API Response - Endpoint: %s, Status: %d, Time: %dms", endpoint, statusCode, executionTime);
        info(message);
    }

    public void logDatabaseOperation(String operation, String entity, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("DB Operation - %s %s: %s", operation, entity, status);
        info(message);
    }

    private String formatMessage(String level, String message) {
        return String.format("[%s] [%s] %s", LocalDateTime.now().format(formatter), level, message);
    }

    private String formatMessage(String level, String className, String methodName, String message) {
        return String.format("[%s] [%s] [%s.%s] %s", LocalDateTime.now().format(formatter), level, className, methodName, message);
    }
}
