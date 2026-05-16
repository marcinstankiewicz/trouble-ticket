package service.troubleticket.service.mapper;

import org.mapstruct.Mapper;
import service.troubleticket.persistence.entity.TroubleTicketEntity;
import service.troubleticket.rs.v1.dto.TroubleTicketClosedResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketCreatedResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketExistingResponse;
import service.troubleticket.rs.v1.dto.TroubleTicketSummary;

@Mapper(componentModel = "spring")
public interface TroubleTicketMapper {

    TroubleTicketCreatedResponse mapToTroubleTicketCreatedResponse(TroubleTicketEntity entity);
    TroubleTicketExistingResponse mapToTroubleTicketExistingResponse(TroubleTicketEntity entity);
    TroubleTicketClosedResponse mapToTroubleTicketClosedResponse(TroubleTicketEntity entity);
    TroubleTicketSummary mapToSummary(TroubleTicketEntity entity);
}
