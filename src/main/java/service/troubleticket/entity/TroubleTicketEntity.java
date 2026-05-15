package service.troubleticket.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder.Default;

@Entity
@Table(name = "trouble_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "notes")
public class TroubleTicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "ticket_id", unique = true, nullable = false, length = 50)
    private String ticketId;
    
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    
    @Column(name = "external_id", nullable = false, length = 100)
    private String externalId;
    
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "troubleTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Default
    private List<NoteEntity> notes = new ArrayList<>();
    
    @Column(name = "unique_key", unique = true, nullable = false, length = 200)
    private String uniqueKey;
}



