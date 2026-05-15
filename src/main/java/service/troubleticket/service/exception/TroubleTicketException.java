package service.troubleticket.service.exception;

import lombok.Getter;

public class TroubleTicketException extends RuntimeException {
    @Getter
    private final String code;
    private final String message;
    
    public TroubleTicketException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
