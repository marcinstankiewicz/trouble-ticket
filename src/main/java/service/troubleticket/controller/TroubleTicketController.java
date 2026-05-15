package service.troubleticket.controller;

import java.util.List;

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
import service.troubleticket.controller.dto.NoteCreateRequest;
import service.troubleticket.controller.dto.NoteResponse;
import service.troubleticket.controller.dto.TroubleTicketCloseStatusRequest;
import service.troubleticket.controller.dto.TroubleTicketCreateRequest;
import service.troubleticket.controller.dto.TroubleTicketResponse;
import service.troubleticket.controller.dto.TroubleTicketSummary;
import service.troubleticket.service.TroubleTicketService;

@RestController
@RequestMapping("/api/v1/troubleTicket")
@RequiredArgsConstructor
public class TroubleTicketController {
    private final TroubleTicketService troubleTicketService;

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

    @GetMapping
    public ResponseEntity<List<TroubleTicketSummary>> listTroubleTickets(
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        List<TroubleTicketSummary> response = troubleTicketService.listTroubleTickets(tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> getTroubleTicketById(
            @PathVariable("id") String ticketId,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.getTroubleTicketById(tenantId, ticketId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> closeTroubleTicket(
            @PathVariable("id") String ticketId,
            @RequestBody TroubleTicketCloseStatusRequest request,
            Authentication authentication) {
        
        String tenantId = getTenantIdFromAuthentication(authentication);
        TroubleTicketResponse response = troubleTicketService.closeTroubleTicket(tenantId, ticketId, request);
        return ResponseEntity.ok(response);
    }

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

    private String getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        return "default-tenant";
    }
}

