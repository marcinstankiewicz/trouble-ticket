package service.troubleticket.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceErrors {
    public static final String WRONG_NEW_STATUS_DESC = "Jedynie status 'new' jest dozwolony dla tej operacji.";
    public static final String WRONG_CLOSED_STATUS_DESC = "Jedynie status 'closed' jest dozwolony dla tej operacji.";
    public static final String TROUBLE_TICKET_NOT_FOUND = "TROUBLE_TICKET_NOT_FOUND";
    public static final String TROUBLE_TICKET_NOT_FOUND_DESC = "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika";
}
