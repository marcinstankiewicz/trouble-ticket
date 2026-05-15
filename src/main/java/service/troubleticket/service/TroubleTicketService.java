package service.troubleticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import service.troubleticket.controller.dto.NoteCreateRequest;
import service.troubleticket.controller.dto.NoteResponse;
import service.troubleticket.controller.dto.TroubleTicketCloseStatusRequest;
import service.troubleticket.controller.dto.TroubleTicketCreateRequest;
import service.troubleticket.controller.dto.TroubleTicketResponse;
import service.troubleticket.controller.dto.TroubleTicketSummary;
import service.troubleticket.persistence.entity.NoteEntity;
import service.troubleticket.persistence.entity.TroubleTicketEntity;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;
import service.troubleticket.persistence.repository.NoteRepository;
import service.troubleticket.persistence.repository.TroubleTicketRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class TroubleTicketService {
    private final TroubleTicketRepository troubleTicketRepository;
    private final NoteRepository noteRepository;
    
    private static final String TICKET_ID_PREFIX = "TT-";
    private static final String NOTE_ID_PREFIX = "NOTE-";
    private static final String TROUBLE_TICKET_NOT_FOUND = "TROUBLE_TICKET_NOT_FOUND";
    private static final String TROUBLE_TICKET_NOT_FOUND_DESC = "Trouble ticket does not exist or tenant for given user is wrong.";

    public TroubleTicketResponse createTroubleTicket(String tenantId, TroubleTicketCreateRequest request) {
        if (!"new".equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", "Status has to be 'new'");
        }
        
        String uniqueKey = generateUniqueKey(tenantId, request.getExternalId());
        var existingTicket = troubleTicketRepository.findByUniqueKey(uniqueKey);
        if (existingTicket.isPresent()) {
            return mapToResponse(existingTicket.get());
        }
        
        String ticketId = generateTicketId();
        TroubleTicketEntity entity = TroubleTicketEntity.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .externalId(request.getExternalId())
            .serviceId(request.getServiceId())
            .description(request.getDescription())
            .status("acknowledged")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .uniqueKey(uniqueKey)
            .build();
        TroubleTicketEntity savedTicket = troubleTicketRepository.save(entity);
        
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

    public List<TroubleTicketSummary> listTroubleTickets(String tenantId) {
        return troubleTicketRepository.findByTenantId(tenantId).stream()
            .map(this::mapToSummary)
            .collect(Collectors.toList());
    }

    public TroubleTicketResponse getTroubleTicketById(String tenantId, String ticketId) {
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException(TROUBLE_TICKET_NOT_FOUND, TROUBLE_TICKET_NOT_FOUND_DESC));
        
        return mapToResponse(ticket);
    }

    public TroubleTicketResponse closeTroubleTicket(String tenantId, String ticketId, 
                                                     TroubleTicketCloseStatusRequest request) {
        if (!"closed".equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", 
                "Only 'closed' status is allowed in this operation.");
        }
        
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException(TROUBLE_TICKET_NOT_FOUND, TROUBLE_TICKET_NOT_FOUND_DESC));
        ticket.setStatus("closed");
        ticket.setUpdatedAt(LocalDateTime.now());
        TroubleTicketEntity updatedTicket = troubleTicketRepository.save(ticket);
        return mapToResponse(updatedTicket);
    }

    public NoteResponse addTroubleTicketNote(String tenantId, String ticketId, NoteCreateRequest request) {
        var ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException(TROUBLE_TICKET_NOT_FOUND, TROUBLE_TICKET_NOT_FOUND_DESC));
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
