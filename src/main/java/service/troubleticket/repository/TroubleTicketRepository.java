package service.troubleticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import service.troubleticket.entity.TroubleTicketEntity;

@Repository
public interface TroubleTicketRepository extends JpaRepository<TroubleTicketEntity, Long> {
    
    Optional<TroubleTicketEntity> findByTicketId(String ticketId);
    
    Optional<TroubleTicketEntity> findByUniqueKey(String uniqueKey);
    
    @Query("SELECT t FROM TroubleTicketEntity t WHERE t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<TroubleTicketEntity> findByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT t FROM TroubleTicketEntity t WHERE t.tenantId = :tenantId AND t.ticketId = :ticketId")
    Optional<TroubleTicketEntity> findByTenantIdAndTicketId(@Param("tenantId") String tenantId, @Param("ticketId") String ticketId);
}
