package service.troubleticket.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("serviceId")
    private Long serviceId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("status")
    private String status;
}

