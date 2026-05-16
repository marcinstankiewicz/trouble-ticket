package service.troubleticket.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiErrorMessages {
    public static final String ERROR_DESC_400 = "Żądanie jest niepoprawne lub wykracza poza dozwolony kontrakt v1.";
    public static final String ERROR_DESC_401 = "Brak uwierzytelnienia albo niepoprawny token.";
    public static final String ERROR_DESC_403 = "Użytkownik jest uwierzytelniony, ale nie ma wymaganych uprawnień do wykonania operacji.";
    public static final String ERROR_DESC_SERVICE404 = "Wskazana usługa nie istnieje, nie jest aktywna albo nie należy do tenant scope użytkownika.";
    public static final String ERROR_DESC_TICKET404 = "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika.";
}
