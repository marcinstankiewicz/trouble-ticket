package service.troubleticket.rs.v1.dto;

import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class TroubleTicketExistingResponse extends TroubleTicketResponse {

    public TroubleTicketExistingResponse(
            String ticketId,
            String externalId,
            Long serviceId,
            String description,
            String status,
            List<NoteResponse> notes
    ) {
        super(ticketId, externalId, serviceId, description, status, notes);
    }
}
