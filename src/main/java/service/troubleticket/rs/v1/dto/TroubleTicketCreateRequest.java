package service.troubleticket.rs.v1.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String externalId;
    private Long serviceId;
    private String description;
    private String status;
    private String note;
}

