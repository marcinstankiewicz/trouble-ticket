package service.troubleticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import service.troubleticket.rs.v1.dto.*;
import service.troubleticket.persistence.entity.NoteEntity;
import service.troubleticket.persistence.entity.TroubleTicketEntity;
import service.troubleticket.persistence.entity.TroubleTicketStatus;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;
import service.troubleticket.persistence.repository.NoteRepository;
import service.troubleticket.persistence.repository.TroubleTicketRepository;

import static service.troubleticket.common.ServiceErrors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleTicketService {
    private final TroubleTicketRepository troubleTicketRepository;
    private final NoteRepository noteRepository;
    
    private static final String TICKET_ID_PREFIX = "TT-";
    private static final String NOTE_ID_PREFIX = "NOTE-";

    @Transactional
    public TroubleTicketResponse createTroubleTicket(String tenantId, TroubleTicketCreateRequest request) {
        if (!TroubleTicketStatus.NEW.getValue().equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", WRONG_NEW_STATUS_DESC);
        }
        
        String uniqueKey = generateUniqueKey(tenantId, request.getExternalId());
        Optional<TroubleTicketEntity> existingTicket = troubleTicketRepository.findByUniqueKey(uniqueKey);
        if (existingTicket.isPresent()) {
            log.info("createTroubleTicket::Trouble ticket already exists for tenantId={} and externalId={}", tenantId, request.getExternalId());
            return mapToTroubleTicketExistingResponse(existingTicket.get());
        }
        
        String ticketId = generateTicketId();
        TroubleTicketEntity entity = TroubleTicketEntity.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .externalId(request.getExternalId())
            .serviceId(request.getServiceId())
            .description(request.getDescription())
            .status(TroubleTicketStatus.ACKNOWLEDGED)
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
        return mapToTroubleTicketCreatedResponse(savedTicket);
    }

    public List<TroubleTicketSummary> listTroubleTickets(String tenantId) {
        return troubleTicketRepository.findByTenantId(tenantId).stream()
            .map(this::mapToSummary)
            .collect(Collectors.toList());
    }

    public TroubleTicketResponse getTroubleTicketById(String tenantId, String ticketId) {
        TroubleTicketEntity ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException(TROUBLE_TICKET_NOT_FOUND, TROUBLE_TICKET_NOT_FOUND_DESC));
        return mapToTroubleTicketCreatedResponse(ticket);
    }

    @Transactional
    public TroubleTicketClosedResponse closeTroubleTicket(String tenantId, String ticketId,
                                                     TroubleTicketCloseStatusRequest request) {
        if (!TroubleTicketStatus.CLOSED.getValue().equals(request.getStatus())) {
            throw new TroubleTicketException("VALIDATION_ERROR", WRONG_CLOSED_STATUS_DESC);
        }
        
        TroubleTicketEntity ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
            .orElseThrow(() -> new TroubleTicketNotFoundException(TROUBLE_TICKET_NOT_FOUND, TROUBLE_TICKET_NOT_FOUND_DESC));
        ticket.setStatus(TroubleTicketStatus.CLOSED);
        ticket.setUpdatedAt(LocalDateTime.now());
        TroubleTicketEntity updatedTicket = troubleTicketRepository.save(ticket);
        return mapToTroubleTicketClosedResponse(updatedTicket);
    }

    @Transactional
    public NoteResponse addTroubleTicketNote(String tenantId, String ticketId, NoteCreateRequest request) {
        TroubleTicketEntity ticket = troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
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
    
    private TroubleTicketCreatedResponse mapToTroubleTicketCreatedResponse(TroubleTicketEntity entity) {
        return new TroubleTicketCreatedResponse(
            entity.getTicketId(),
            entity.getExternalId(),
            entity.getServiceId(),
            entity.getDescription(),
            entity.getStatus().getValue(),
            entity.getNotes().stream()
                .map(this::mapNoteToResponse)
                .collect(Collectors.toList())
        );
    }

    private TroubleTicketExistingResponse mapToTroubleTicketExistingResponse(TroubleTicketEntity entity) {
        return new TroubleTicketExistingResponse(
                entity.getTicketId(),
                entity.getExternalId(),
                entity.getServiceId(),
                entity.getDescription(),
                entity.getStatus().getValue(),
                entity.getNotes().stream()
                        .map(this::mapNoteToResponse)
                        .collect(Collectors.toList())
        );
    }

    private TroubleTicketClosedResponse mapToTroubleTicketClosedResponse(TroubleTicketEntity entity) {
        return new TroubleTicketClosedResponse(
                entity.getTicketId(),
                entity.getExternalId(),
                entity.getServiceId(),
                entity.getDescription(),
                entity.getStatus().getValue(),
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
            entity.getStatus().getValue()
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
