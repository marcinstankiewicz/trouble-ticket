package service.troubleticket.config.controlleradvice;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import service.troubleticket.rs.v1.dto.ErrorResponse;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;

import static service.troubleticket.common.ExceptionErrors.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TroubleTicketException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicket400Exception(
            TroubleTicketException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ERROR_DESC_400, generateRequestId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument400Exception(
            IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", ERROR_DESC_400, generateRequestId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationCredentialsNotFound401Exception(
            AuthenticationCredentialsNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", ERROR_DESC_401, generateRequestId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(TroubleTicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicketNotFound404Exception(
            TroubleTicketNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ERROR_DESC_TICKET404, generateRequestId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric500Exception(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Internal server error.",
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
