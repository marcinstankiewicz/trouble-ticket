package service.troubleticket.controller;

import java.util.List;
import java.util.UUID;

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
import service.troubleticket.dto.NoteCreateRequest;
import service.troubleticket.dto.NoteResponse;
import service.troubleticket.dto.TroubleTicketCloseStatusRequest;
import service.troubleticket.dto.TroubleTicketCreateRequest;
import service.troubleticket.dto.TroubleTicketResponse;
import service.troubleticket.dto.TroubleTicketSummary;
import service.troubleticket.service.TroubleTicketService;

@RestController
@RequestMapping("/api/v1/troubleTicket")
@RequiredArgsConstructor
public class TroubleTicketController {
    private final TroubleTicketService troubleTicketService;
    
    /**
     * POST /api/v1/troubleTicket - Utwórz nowe zgłoszenie Trouble Ticket
     * 
     * Tworzy nowe zgłoszenie przypisane do tenant scope wynikającego z Bearer tokenu.
     * Zwraca 201 dla nowego zgłoszenia, 200 dla istniejącego na podstawie idempotencji.
     */
    @PostMapping
    public ResponseEntity<TroubleTicketResponse> createTroubleTicket(
            @RequestBody TroubleTicketCreateRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.createTroubleTicket(tenantId, request);
        
        String locationHeader = "/api/v1/troubleTicket/" + response.getId();
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.LOCATION, locationHeader)
            .body(response);
    }
    
    /**
     * GET /api/v1/troubleTicket - Listuj zgłoszenia Trouble Ticket
     * 
     * Zwraca minimalną listę zgłoszeń widocznych dla uwierzytelnionego użytkownika.
     */
    @GetMapping
    public ResponseEntity<List<TroubleTicketSummary>> listTroubleTickets(
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        List<TroubleTicketSummary> response = troubleTicketService.listTroubleTickets(tenantId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/troubleTicket/{id} - Pobierz pojedyncze zgłoszenie Trouble Ticket
     * 
     * Zwraca pełną reprezentację zgłoszenia widocznego dla uwierzytelnionego użytkownika.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> getTroubleTicketById(
            @PathVariable("id") String ticketId,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.getTroubleTicketById(tenantId, ticketId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PATCH /api/v1/troubleTicket/{id} - Zamknij zgłoszenie Trouble Ticket
     * 
     * Umożliwia publicznemu klientowi API zmianę statusu wyłącznie na 'closed'.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> closeTroubleTicket(
            @PathVariable("id") String ticketId,
            @RequestBody TroubleTicketCloseStatusRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.closeTroubleTicket(tenantId, ticketId, request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/troubleTicket/{id}/note - Dodaj notatkę do zgłoszenia Trouble Ticket
     * 
     * Tworzy nową notatkę dla istniejącego zgłoszenia widocznego w tenant scope użytkownika.
     */
    @PostMapping("/{id}/note")
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
    
    /**
     * Extracts tenant ID from Spring Security Authentication context.
     * Tenant ID is set in JwtTokenFilter as the principal.
     */
    private String getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        // Fallback tenant ID for development
        return "default-tenant";
    }
}

