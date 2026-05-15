package service.troubleticket.controller.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String externalId;
    private Long serviceId;
    private String description;
    private String status;
    private List<NoteResponse> notes;
}

