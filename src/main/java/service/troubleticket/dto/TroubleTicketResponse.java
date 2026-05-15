package service.troubleticket.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("serviceId")
    private Long serviceId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("notes")
    private List<NoteResponse> notes;
}

