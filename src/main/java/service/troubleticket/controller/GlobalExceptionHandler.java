package service.troubleticket.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import service.troubleticket.dto.ErrorResponse;
import service.troubleticket.exception.TroubleTicketException;
import service.troubleticket.exception.TroubleTicketNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TroubleTicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicketNotFoundException(
            TroubleTicketNotFoundException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            generateRequestId()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TroubleTicketException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicketException(
            TroubleTicketException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            generateRequestId()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNAUTHORIZED",
            "Brak poprawnego Bearer tokenu.",
            generateRequestId()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            generateRequestId()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Wewnętrzny błąd serwera.",
            generateRequestId()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
