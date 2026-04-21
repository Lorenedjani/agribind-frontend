package com.agribind.communication.exception;

/**
 * Exception thrown when audio file upload fails
 */
public class AudioUploadException extends RuntimeException {

    public AudioUploadException(String message) {
        super(message);
    }

    public AudioUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}