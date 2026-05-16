package service.troubleticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import service.troubleticket.persistence.entity.NoteEntity;
import service.troubleticket.persistence.entity.TroubleTicketEntity;
import service.troubleticket.persistence.entity.TroubleTicketStatus;
import service.troubleticket.persistence.repository.NoteRepository;
import service.troubleticket.persistence.repository.TroubleTicketRepository;
import service.troubleticket.rs.v1.dto.*;
import service.troubleticket.service.exception.TroubleTicketException;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;
import service.troubleticket.service.mapper.NoteMapper;
import service.troubleticket.service.mapper.TroubleTicketMapper;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TroubleTicketServiceTest {

    @Mock
    private TroubleTicketRepository troubleTicketRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private TroubleTicketMapper mapper;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private TroubleTicketService troubleTicketService;

    private String tenantId;
    private String externalId;
    private String ticketId;
    private TroubleTicketCreateRequest createRequest;
    private TroubleTicketEntity ticketEntity;

    @BeforeEach
    void setUp() {
        tenantId = "tenant-123";
        externalId = "ext-456";
        ticketId = "TT-1234567890";
        createRequest = new TroubleTicketCreateRequest();
        createRequest.setExternalId(externalId);
        createRequest.setServiceId(1L);
        createRequest.setDescription("Test description");
        createRequest.setStatus("new");
        createRequest.setNote("Test note");
        ticketEntity = TroubleTicketEntity.builder()
                .id(1L)
                .ticketId(ticketId)
                .tenantId(tenantId)
                .externalId(externalId)
                .serviceId(1L)
                .description("Test description")
                .status(TroubleTicketStatus.ACKNOWLEDGED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .uniqueKey(tenantId + "|" + externalId)
                .notes(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createTroubleTicket - should create new ticket with status 'new'")
    void testCreateTroubleTicketSuccessfully() {
        // Arrange
        when(troubleTicketRepository.findByUniqueKey(anyString())).thenReturn(Optional.empty());
        when(troubleTicketRepository.save(any(TroubleTicketEntity.class))).thenReturn(ticketEntity);
        when(noteRepository.save(any(NoteEntity.class))).thenReturn(new NoteEntity());
        
        TroubleTicketCreatedResponse expectedResponse = new TroubleTicketCreatedResponse(
                ticketId, externalId, 1L, "Test description", "acknowledged", new ArrayList<>()
        );
        when(mapper.mapToTroubleTicketCreatedResponse(any())).thenReturn(expectedResponse);

        // Act
        TroubleTicketResponse response = troubleTicketService.createTroubleTicket(tenantId, createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(ticketId, response.getId());
        verify(troubleTicketRepository).findByUniqueKey(tenantId + "|" + externalId);
        verify(troubleTicketRepository).save(any(TroubleTicketEntity.class));
        verify(noteRepository).save(any(NoteEntity.class));
    }

    @Test
    @DisplayName("createTroubleTicket - should return existing ticket if already exists")
    void testCreateTroubleTicketWithExistingTicket() {
        // Arrange
        TroubleTicketEntity existingTicket = TroubleTicketEntity.builder()
                .id(1L)
                .ticketId(ticketId)
                .tenantId(tenantId)
                .externalId(externalId)
                .uniqueKey(tenantId + "|" + externalId)
                .build();

        when(troubleTicketRepository.findByUniqueKey(anyString())).thenReturn(Optional.of(existingTicket));
        
        TroubleTicketExistingResponse expectedResponse = new TroubleTicketExistingResponse(
                ticketId, externalId, 1L, "Test description", "acknowledged", new ArrayList<>()
        );
        when(mapper.mapToTroubleTicketExistingResponse(existingTicket)).thenReturn(expectedResponse);

        // Act
        TroubleTicketResponse response = troubleTicketService.createTroubleTicket(tenantId, createRequest);

        // Assert
        assertNotNull(response);
        assertInstanceOf(TroubleTicketExistingResponse.class, response);
        verify(troubleTicketRepository).findByUniqueKey(tenantId + "|" + externalId);
        verify(mapper).mapToTroubleTicketExistingResponse(existingTicket);
        verify(troubleTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTroubleTicket - should throw exception if status is not 'new'")
    void testCreateTroubleTicketWithWrongStatus() {
        // Arrange
        createRequest.setStatus("acknowledged");

        // Act & Assert
        assertThrows(TroubleTicketException.class, () -> 
            troubleTicketService.createTroubleTicket(tenantId, createRequest)
        );
        verify(troubleTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("listTroubleTickets - should return list of tickets for tenant")
    void testListTroubleTicketsSuccessfully() {
        // Arrange
        List<TroubleTicketEntity> tickets = Arrays.asList(ticketEntity);
        when(troubleTicketRepository.findByTenantId(tenantId)).thenReturn(tickets);
        
        TroubleTicketSummary summary = new TroubleTicketSummary(externalId, 1L, "Test description", "acknowledged");
        when(mapper.mapToSummary(any())).thenReturn(summary);

        // Act
        List<TroubleTicketSummary> result = troubleTicketService.listTroubleTickets(tenantId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(externalId, result.get(0).getExternalId());
        verify(troubleTicketRepository).findByTenantId(tenantId);
        verify(mapper).mapToSummary(ticketEntity);
    }

    @Test
    @DisplayName("listTroubleTickets - should return empty list when no tickets exist")
    void testListTroubleTicketsEmptyList() {
        // Arrange
        when(troubleTicketRepository.findByTenantId(tenantId)).thenReturn(new ArrayList<>());

        // Act
        List<TroubleTicketSummary> result = troubleTicketService.listTroubleTickets(tenantId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(troubleTicketRepository).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("getTroubleTicketById - should return ticket when exists")
    void testGetTroubleTicketByIdSuccessfully() {
        // Arrange
        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.of(ticketEntity));
        
        TroubleTicketCreatedResponse expectedResponse = new TroubleTicketCreatedResponse(
                ticketId, externalId, 1L, "Test description", "acknowledged", new ArrayList<>()
        );
        when(mapper.mapToTroubleTicketCreatedResponse(ticketEntity)).thenReturn(expectedResponse);

        // Act
        TroubleTicketResponse result = troubleTicketService.getTroubleTicketById(tenantId, ticketId);

        // Assert
        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        verify(troubleTicketRepository).findByTenantIdAndTicketId(tenantId, ticketId);
    }

    @Test
    @DisplayName("getTroubleTicketById - should throw exception when ticket not found")
    void testGetTroubleTicketByIdNotFound() {
        // Arrange
        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TroubleTicketNotFoundException.class, () -> 
            troubleTicketService.getTroubleTicketById(tenantId, ticketId)
        );
        verify(troubleTicketRepository).findByTenantIdAndTicketId(tenantId, ticketId);
    }

    @Test
    @DisplayName("closeTroubleTicket - should close ticket successfully")
    void testCloseTroubleTicketSuccessfully() {
        // Arrange
        TroubleTicketCloseStatusRequest closeRequest = new TroubleTicketCloseStatusRequest("closed");
        
        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.of(ticketEntity));
        when(troubleTicketRepository.save(any(TroubleTicketEntity.class))).thenReturn(ticketEntity);
        
        TroubleTicketClosedResponse expectedResponse = new TroubleTicketClosedResponse(
                ticketId, externalId, 1L, "Test description", "closed", new ArrayList<>()
        );
        when(mapper.mapToTroubleTicketClosedResponse(any())).thenReturn(expectedResponse);

        // Act
        TroubleTicketClosedResponse result = troubleTicketService.closeTroubleTicket(tenantId, ticketId, closeRequest);

        // Assert
        assertNotNull(result);
        assertEquals("closed", result.getStatus());
        verify(troubleTicketRepository).findByTenantIdAndTicketId(tenantId, ticketId);
        verify(troubleTicketRepository).save(any(TroubleTicketEntity.class));
    }

    @Test
    @DisplayName("closeTroubleTicket - should throw exception if status is not 'closed'")
    void testCloseTroubleTicketWithWrongStatus() {
        // Arrange
        TroubleTicketCloseStatusRequest closeRequest = new TroubleTicketCloseStatusRequest("acknowledged");

        // Act & Assert
        assertThrows(TroubleTicketException.class, () -> 
            troubleTicketService.closeTroubleTicket(tenantId, ticketId, closeRequest)
        );
        verify(troubleTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("closeTroubleTicket - should throw exception when ticket not found")
    void testCloseTroubleTicketNotFound() {
        // Arrange
        TroubleTicketCloseStatusRequest closeRequest = new TroubleTicketCloseStatusRequest("closed");
        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TroubleTicketNotFoundException.class, () -> 
            troubleTicketService.closeTroubleTicket(tenantId, ticketId, closeRequest)
        );
    }

    @Test
    @DisplayName("addTroubleTicketNote - should add note to ticket successfully")
    void testAddTroubleTicketNoteSuccessfully() {
        // Arrange
        NoteCreateRequest noteRequest = new NoteCreateRequest("Test note text");
        NoteEntity noteEntity = NoteEntity.builder()
                .id(1L)
                .noteId("NOTE-abc123")
                .text("Test note text")
                .createdAt(LocalDateTime.now())
                .troubleTicket(ticketEntity)
                .build();

        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.of(ticketEntity));
        when(noteRepository.save(any(NoteEntity.class))).thenReturn(noteEntity);
        when(troubleTicketRepository.save(any(TroubleTicketEntity.class))).thenReturn(ticketEntity);
        
        NoteResponse expectedResponse = new NoteResponse("NOTE-abc123", "Test note text", LocalDateTime.now());
        when(noteMapper.mapNoteToResponse(noteEntity)).thenReturn(expectedResponse);

        // Act
        NoteResponse result = troubleTicketService.addTroubleTicketNote(tenantId, ticketId, noteRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test note text", result.getText());
        verify(troubleTicketRepository).findByTenantIdAndTicketId(tenantId, ticketId);
        verify(noteRepository).save(any(NoteEntity.class));
        verify(troubleTicketRepository).save(ticketEntity);
    }

    @Test
    @DisplayName("addTroubleTicketNote - should throw exception when ticket not found")
    void testAddTroubleTicketNoteTicketNotFound() {
        // Arrange
        NoteCreateRequest noteRequest = new NoteCreateRequest("Test note text");
        when(troubleTicketRepository.findByTenantIdAndTicketId(tenantId, ticketId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TroubleTicketNotFoundException.class, () -> 
            troubleTicketService.addTroubleTicketNote(tenantId, ticketId, noteRequest)
        );
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTroubleTicket - should generate unique ticketId")
    void testCreateTroubleTicketGeneratesUniqueId() {
        // Arrange
        when(troubleTicketRepository.findByUniqueKey(anyString())).thenReturn(Optional.empty());
        when(troubleTicketRepository.save(any(TroubleTicketEntity.class))).thenReturn(ticketEntity);
        when(noteRepository.save(any(NoteEntity.class))).thenReturn(new NoteEntity());
        when(mapper.mapToTroubleTicketCreatedResponse(any())).thenReturn(
                new TroubleTicketCreatedResponse(ticketId, externalId, 1L, "Test description", "acknowledged", new ArrayList<>())
        );

        // Act
        troubleTicketService.createTroubleTicket(tenantId, createRequest);

        // Assert
        ArgumentCaptor<TroubleTicketEntity> captor = ArgumentCaptor.forClass(TroubleTicketEntity.class);
        verify(troubleTicketRepository).save(captor.capture());
        
        TroubleTicketEntity savedEntity = captor.getValue();
        assertNotNull(savedEntity.getTicketId());
        assertTrue(savedEntity.getTicketId().startsWith("TT-"));
    }

    @Test
    @DisplayName("listTroubleTickets - should fetch multiple tickets")
    void testListTroubleTicketsMultiple() {
        // Arrange
        TroubleTicketEntity ticket2 = TroubleTicketEntity.builder()
                .id(2L)
                .ticketId("TT-9876543210")
                .tenantId(tenantId)
                .externalId("ext-789")
                .serviceId(2L)
                .description("Test description 2")
                .status(TroubleTicketStatus.CLOSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .uniqueKey(tenantId + "|ext-789")
                .notes(new ArrayList<>())
                .build();

        List<TroubleTicketEntity> tickets = Arrays.asList(ticketEntity, ticket2);
        when(troubleTicketRepository.findByTenantId(tenantId)).thenReturn(tickets);
        when(mapper.mapToSummary(any())).thenReturn(new TroubleTicketSummary());

        // Act
        List<TroubleTicketSummary> result = troubleTicketService.listTroubleTickets(tenantId);

        // Assert
        assertEquals(2, result.size());
        verify(mapper, times(2)).mapToSummary(any());
    }
}
