package service.troubleticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import service.troubleticket.dto.NoteCreateRequest;
import service.troubleticket.dto.NoteResponse;
import service.troubleticket.dto.TroubleTicketCloseStatusRequest;
import service.troubleticket.dto.TroubleTicketCreateRequest;
import service.troubleticket.dto.TroubleTicketResponse;
import service.troubleticket.dto.TroubleTicketSummary;
import service.troubleticket.entity.NoteEntity;
import service.troubleticket.entity.TroubleTicketEntity;
import service.troubleticket.exception.TroubleTicketException;
import service.troubleticket.exception.TroubleTicketNotFoundException;
import service.troubleticket.repository.NoteRepository;
import service.troubleticket.repository.TroubleTicketRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class TroubleTicketService {
    private final TroubleTicketRepository troubleTicketRepository;
    private final NoteRepository noteRepository;
    
    private static final String TICKET_ID_PREFIX = "TT-";
    private static final String NOTE_ID_PREFIX = "NOTE-";
    
    /**
     * Tworzy lub zwraca istniejące zgłoszenie na podstawie idempotencji (tenantId, externalId).
     * 
     * Status "new" jest zmapowany na "acknowledged" dla nowego zgłoszenia.
     */
    public TroubleTicketResponse createTroubleTicket(String tenantId, TroubleTicketCreateRequest request) {
        // Walidacja statusu
        if (!"new".equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", "Status musi być równy 'new'");
        }
        
        // Klucz unikatowości dla (tenantId, externalId)
        String uniqueKey = generateUniqueKey(tenantId, request.getExternalId());
        
        // Sprawdzenie istniejącego zgłoszenia
        var existingTicket = troubleTicketRepository.findByUniqueKey(uniqueKey);
        if (existingTicket.isPresent()) {
            return mapToResponse(existingTicket.get());
        }
        
        // Tworzenie nowego zgłoszenia
        String ticketId = generateTicketId();
        TroubleTicketEntity entity = TroubleTicketEntity.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .externalId(request.getExternalId())
            .serviceId(request.getServiceId())
            .description(request.getDescription())
            .status("acknowledged") // Status zmapowany z "new" na "acknowledged"
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .uniqueKey(uniqueKey)
            .build();
        
        TroubleTicketEntity savedTicket = troubleTicketRepository.save(entity);
        
        // Tworzenie pierwszej notatki
        NoteEntity noteEntity = NoteEntity.builder()
            .noteId(generateNoteId())
            .text(request.getNote())
            .createdAt(LocalDateTime.now())
            .troubleTicket(savedTicket)
            .build();
        
        NoteEntity savedNote = noteRepository.save(noteEntity);
        savedTicket.getNotes().add(savedNote);
        
        return mapToResponse(savedTicket);
    }
    
    /**
     * Zwraca listę zgłoszeń dla danego tenant.
     */
    public List<TroubleTicketSummary> listTroubleTickets(String tenantId) {
        return troubleTicketRepository.findByTenantId(tenantId).stream()
            .map(this::mapToSummary)
            .collect(Collectors.toList());
    }
    
    /**
     * Zwraca szczegóły pojedynczego zgłoszenia.
     */
    public TroubleTicketResponse getTroubleTicketById(String tenantId, String ticketId) {
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException("TROUBLE_TICKET_NOT_FOUND", 
                "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika."));
        
        return mapToResponse(ticket);
    }
    
    /**
     * Zamyka zgłoszenie (zmiana statusu na "closed").
     */
    public TroubleTicketResponse closeTroubleTicket(String tenantId, String ticketId, 
                                                     TroubleTicketCloseStatusRequest request) {
        // Walidacja statusu
        if (!"closed".equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", 
                "Jedynie status 'closed' jest dozwolony dla tej operacji");
        }
        
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException("TROUBLE_TICKET_NOT_FOUND", 
                "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika."));
        
        ticket.setStatus("closed");
        ticket.setUpdatedAt(LocalDateTime.now());
        
        TroubleTicketEntity updatedTicket = troubleTicketRepository.save(ticket);
        
        return mapToResponse(updatedTicket);
    }
    
    /**
     * Dodaje notatkę do zgłoszenia.
     */
    public NoteResponse addTroubleTicketNote(String tenantId, String ticketId, NoteCreateRequest request) {
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException("TROUBLE_TICKET_NOT_FOUND", 
                "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika."));
        
        NoteEntity noteEntity = NoteEntity.builder()
            .noteId(generateNoteId())
            .text(request.getText())
            .createdAt(LocalDateTime.now())
            .troubleTicket(ticket)
            .build();
        
        NoteEntity savedNote = noteRepository.save(noteEntity);
        ticket.getNotes().add(savedNote);
        troubleTicketRepository.save(ticket);
        
        return mapNoteToResponse(savedNote);
    }
    
    // ===== Helper Methods =====
    
    private TroubleTicketResponse mapToResponse(TroubleTicketEntity entity) {
        return new TroubleTicketResponse(
            entity.getTicketId(),
            entity.getExternalId(),
            entity.getServiceId(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getNotes().stream()
                .map(this::mapNoteToResponse)
                .collect(Collectors.toList())
        );
    }
    
    private TroubleTicketSummary mapToSummary(TroubleTicketEntity entity) {
        return new TroubleTicketSummary(
            entity.getExternalId(),
            entity.getServiceId(),
            entity.getDescription(),
            entity.getStatus()
        );
    }
    
    private NoteResponse mapNoteToResponse(NoteEntity entity) {
        return new NoteResponse(
            entity.getNoteId(),
            entity.getText(),
            entity.getCreatedAt()
        );
    }
    
    private String generateTicketId() {
        return TICKET_ID_PREFIX + System.currentTimeMillis();
    }
    
    private String generateNoteId() {
        return NOTE_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateUniqueKey(String tenantId, String externalId) {
        return tenantId + "|" + externalId;
    }
}
