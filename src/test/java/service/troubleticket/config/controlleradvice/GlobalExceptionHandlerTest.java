package service.troubleticket.config.controlleradvice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import service.troubleticket.common.ApiErrorMessages;
import service.troubleticket.rs.v1.dto.ErrorResponse;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static service.troubleticket.common.ErrorCodes.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("TroubleTicketException should return 400 with VALIDATION_ERROR code")
    void shouldReturn400ForTroubleTicketException() {
        // Arrange
        TroubleTicketException ex = new TroubleTicketException("INVALID_STATUS", "Invalid status");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleTroubleTicket400Exception(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALIDATION_ERROR, response.getBody().getCode());
        assertEquals(ApiErrorMessages.ERROR_DESC_400, response.getBody().getMessage());
        assertTrue(response.getBody().getRequestId().startsWith("req-"));
    }

    @Test
    @DisplayName("TroubleTicketNotFoundException should return 404 with TROUBLE_TICKET_NOT_FOUND code")
    void shouldReturn404ForTroubleTicketNotFoundException() {
        // Arrange
        TroubleTicketNotFoundException ex = new TroubleTicketNotFoundException("NOT_FOUND", "Ticket not found");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleTroubleTicketNotFound404Exception(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TROUBLE_TICKET_NOT_FOUND, response.getBody().getCode());
        assertEquals(ApiErrorMessages.ERROR_DESC_TICKET404, response.getBody().getMessage());
        assertTrue(response.getBody().getRequestId().startsWith("req-"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException should return 400 with field error message")
    void shouldReturn400ForMethodArgumentNotValidExceptionWithFieldError() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "externalId", "Pole externalId ma niedozwoloną wartość dla tej operacji."));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethods()[0], -1),
                bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument400Exception(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALIDATION_ERROR, response.getBody().getCode());
        assertEquals("Pole externalId ma niedozwoloną wartość dla tej operacji.", response.getBody().getMessage());
        assertTrue(response.getBody().getRequestId().startsWith("req-"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException without field error should return default 400 message")
    void shouldReturn400ForMethodArgumentNotValidExceptionWithoutFieldError() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethods()[0], -1),
                bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument400Exception(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALIDATION_ERROR, response.getBody().getCode());
        assertEquals(ApiErrorMessages.ERROR_DESC_400, response.getBody().getMessage());
        assertTrue(response.getBody().getRequestId().startsWith("req-"));
    }
}
