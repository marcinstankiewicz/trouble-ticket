package service.troubleticket.rs.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.troubleticket.config.controlleradvice.GlobalExceptionHandler;
import service.troubleticket.rs.v1.dto.NoteCreateRequest;
import service.troubleticket.rs.v1.dto.NoteResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketCloseStatusRequest;
import service.troubleticket.rs.v1.dto.TroubleTicketClosedResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketCreateRequest;
import service.troubleticket.rs.v1.dto.TroubleTicketCreatedResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketExistingResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketSummary;
import service.troubleticket.service.TroubleTicketService;
import service.troubleticket.service.exception.TroubleTicketNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TroubleTicketControllerTest {

    private ObjectMapper objectMapper;

    @Mock
    private TroubleTicketService troubleTicketService;

    @InjectMocks
    private TroubleTicketController troubleTicketController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(troubleTicketController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    @DisplayName("POST /api/v1/troubleTicket -> 201 for newly created ticket")
    void createTroubleTicketReturnsCreated() throws Exception {
        // Arrange
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest(
                "ext-1", 10L, "desc", "new", "note");
        TroubleTicketCreatedResponse response = new TroubleTicketCreatedResponse(
                "TT-1", "ext-1", 10L, "desc", "acknowledged", List.of());
        when(troubleTicketService.createTroubleTicket(eq("tenant-1"), any(TroubleTicketCreateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/troubleTicket")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/troubleTicket/TT-1"))
                .andExpect(jsonPath("$.id").value("TT-1"));
    }

    @Test
    @DisplayName("POST /api/v1/troubleTicket -> 200 for existing ticket")
    void createTroubleTicketReturnsOkWhenExisting() throws Exception {
        // Arrange
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest(
                "ext-1", 10L, "desc", "new", "note");
        TroubleTicketExistingResponse response = new TroubleTicketExistingResponse(
                "TT-1", "ext-1", 10L, "desc", "acknowledged", List.of());
        when(troubleTicketService.createTroubleTicket(eq("tenant-1"), any(TroubleTicketCreateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/troubleTicket")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "/api/v1/troubleTicket/TT-1"));
    }

    @Test
    @DisplayName("POST /api/v1/troubleTicket -> 400 for validation errors")
    void createTroubleTicketValidationError() throws Exception {
        // Arrange
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest(
                "", 10L, "desc", "new", "note");

        // Act & Assert
        mockMvc.perform(post("/api/v1/troubleTicket")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verify(troubleTicketService, never()).createTroubleTicket(any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/troubleTicket -> 200 with ticket list")
    void listTroubleTicketsReturnsOk() throws Exception {
        // Arrange
        when(troubleTicketService.listTroubleTickets("tenant-1"))
                .thenReturn(List.of(new TroubleTicketSummary("ext-1", 10L, "desc", "new")));

        // Act & Assert
        mockMvc.perform(get("/api/v1/troubleTicket")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("ext-1"));
    }

    @Test
    @DisplayName("GET /api/v1/troubleTicket/{id} -> 404 when ticket does not exist")
    void getTroubleTicketByIdNotFound() throws Exception {
        // Arrange
        when(troubleTicketService.getTroubleTicketById("tenant-1", "TT-404"))
                .thenThrow(new TroubleTicketNotFoundException("NOT_FOUND", "not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/troubleTicket/TT-404")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TROUBLE_TICKET_NOT_FOUND"));
    }

    @Test
    @DisplayName("PATCH /api/v1/troubleTicket/{id} -> 200 when closed")
    void closeTroubleTicketReturnsOk() throws Exception {
        // Arrange
        TroubleTicketClosedResponse response = new TroubleTicketClosedResponse(
                "TT-1", "ext-1", 10L, "desc", "closed", List.of());
        when(troubleTicketService.closeTroubleTicket(eq("tenant-1"), eq("TT-1"), any(TroubleTicketCloseStatusRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/troubleTicket/TT-1")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TroubleTicketCloseStatusRequest("closed"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("closed"));
    }

    @Test
    @DisplayName("POST /api/v1/troubleTicket/{id}/note -> 201 with Location")
    void addTroubleTicketNoteReturnsCreated() throws Exception {
        // Arrange
        NoteResponse response = new NoteResponse("NOTE-1", "note text", LocalDateTime.now());
        when(troubleTicketService.addTroubleTicketNote(eq("tenant-1"), eq("TT-1"), any(NoteCreateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/troubleTicket/TT-1/note")
                        .principal(new TestingAuthenticationToken("tenant-1", "test-credentials"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NoteCreateRequest("note text"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/troubleTicket/TT-1/note/NOTE-1"))
                .andExpect(jsonPath("$.id").value("NOTE-1"));
    }
}
