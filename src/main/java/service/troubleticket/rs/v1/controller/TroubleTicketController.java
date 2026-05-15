package service.troubleticket.rs.v1.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import service.troubleticket.rs.v1.dto.*;
import service.troubleticket.service.TroubleTicketService;

@RestController
@RequestMapping("/api/v1/troubleTicket")
@RequiredArgsConstructor
public class TroubleTicketController {
    private final TroubleTicketService troubleTicketService;

    @PostMapping
    @Operation(
            tags = "TroubleTicket",
            operationId = "createTroubleTicket",
            summary = "Utwórz zgłoszenie Trouble Ticket",
            description = """
            Tworzy nowe zgłoszenie przypisane do tenant scope wynikającego z Bearer tokenu.

            Klient publicznego API może przekazać wyłącznie status `new`.
            Jeżeli istnieje już zgłoszenie o tym samym `(tenantId, externalId)`,
            API zwraca `200 OK` z istniejącą reprezentacją zasobu.

            Dla nowo utworzonego zgłoszenia odpowiedź może już zwracać status
            `acknowledged`, zgodnie z przepływem przetwarzania po stronie SOZ.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Zgłoszenie utworzone.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TroubleTicketCreatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Zwrócono istniejące zgłoszenie na podstawie idempotencji `(tenantId, externalId)`.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TroubleTicketExistingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Service Not Found")
    })
    public ResponseEntity<TroubleTicketResponse> createTroubleTicket(
            @RequestBody TroubleTicketCreateRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.createTroubleTicket(tenantId, request);
        String locationHeader = "/api/v1/troubleTicket/" + response.getId();
        if (response instanceof TroubleTicketExistingResponse) {
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.LOCATION, locationHeader)
                    .body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.LOCATION, locationHeader)
                    .body(response);
        }
    }

    @GetMapping
    @Operation(
            tags = "TroubleTicket",
            operationId = "listTroubleTickets",
            summary = "Listuj zgłoszenia Trouble Ticket",
            description = """
            Zwraca minimalną listę zgłoszeń widocznych dla uwierzytelnionego użytkownika.

            W wersji v1 odpowiedź nie wspiera paginacji ani filtrowania.

            Zakres danych jest ograniczony do:
            `externalId`, `serviceId`, `description`, `status`.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista zgłoszeń dostępnych w tenant scope użytkownika.",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TroubleTicketSummary.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<TroubleTicketSummary>> listTroubleTickets(
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        List<TroubleTicketSummary> response = troubleTicketService.listTroubleTickets(tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            tags = "TroubleTicket",
            operationId = "getTroubleTicketById",
            summary = "Pobierz pojedyncze zgłoszenie Trouble Ticket",
            description = """
            Zwraca pełną reprezentację zgłoszenia widocznego
            dla uwierzytelnionego użytkownika.

            tenant scope wynika wyłącznie z Bearer tokenu.

            Dla zasobu nieistniejącego albo niewidocznego
            w tenant scope należy zwrócić `404`.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pełna reprezentacja zgłoszenia.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TroubleTicketResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Trouble Ticket not found")
    })
    public ResponseEntity<TroubleTicketResponse> getTroubleTicketById(
            @PathVariable("id") String ticketId,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.getTroubleTicketById(tenantId, ticketId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            tags = "TroubleTicket",
            operationId = "closeTroubleTicket",
            summary = "Zamknij zgłoszenie Trouble Ticket",
            description = """
            Umożliwia publicznemu klientowi API zmianę statusu
            wyłącznie na `closed`.

            Pozostałe przejścia statusów są poza zakresem
            tej specyfikacji i muszą być odrzucone jako `400`.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Zgłoszenie zostało zamknięte.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TroubleTicketClosedResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Trouble Ticket not found")
    })
    public ResponseEntity<TroubleTicketClosedResponse> closeTroubleTicket(
            @PathVariable("id") String ticketId,
            @RequestBody TroubleTicketCloseStatusRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketClosedResponse response = troubleTicketService.closeTroubleTicket(tenantId, ticketId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/note")
    @Operation(
            tags = "TroubleTicketNote",
            operationId = "addTroubleTicketNote",
            summary = "Dodaj notatkę do zgłoszenia Trouble Ticket",
            description = """
            Tworzy nową notatkę dla istniejącego zgłoszenia
            widocznego w tenant scope użytkownika.

            Operacja tworzy subresource notatki i nie służy
            do zmiany statusu zgłoszenia.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Notatka została dodana.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Trouble Ticket not found")
    })
    public ResponseEntity<NoteResponse> addTroubleTicketNote(
            @PathVariable("id") String ticketId,
            @RequestBody NoteCreateRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        NoteResponse response = troubleTicketService.addTroubleTicketNote(tenantId, ticketId, request);
        String locationHeader = "/api/v1/troubleTicket/" + ticketId + "/note/" + response.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.LOCATION, locationHeader)
            .body(response);
    }

    private String getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        return "default-tenant";
    }
}

