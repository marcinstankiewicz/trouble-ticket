package service.troubleticket.config.controlleradvice;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import service.troubleticket.rs.v1.dto.ErrorResponse;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;

import static service.troubleticket.common.ApiErrorMessages.*;
import static service.troubleticket.common.ErrorCodes.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TroubleTicketException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicket400Exception(
            TroubleTicketException ex) {
        ErrorResponse errorResponse = new ErrorResponse(VALIDATION_ERROR, ERROR_DESC_400, generateRequestId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument400Exception(
            MethodArgumentNotValidException ex) {
        String defaultMessage = ex.getBindingResult().getFieldError() != null ? ex.getBindingResult().getFieldError().getDefaultMessage() : ERROR_DESC_400;
        ErrorResponse errorResponse = new ErrorResponse(VALIDATION_ERROR, defaultMessage, generateRequestId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(TroubleTicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTroubleTicketNotFound404Exception(
            TroubleTicketNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(TROUBLE_TICKET_NOT_FOUND, ERROR_DESC_TICKET404, generateRequestId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGeneric500Exception(Exception ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//            "INTERNAL_SERVER_ERROR",
//            "Internal server error.",
//            generateRequestId()
//        );
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
    
    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
