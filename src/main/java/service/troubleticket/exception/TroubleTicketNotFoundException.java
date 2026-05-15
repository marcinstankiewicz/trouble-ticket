package service.troubleticket.exception;

public class TroubleTicketNotFoundException extends TroubleTicketException {
    public TroubleTicketNotFoundException(String code, String message) {
        super(code, message);
    }
}
