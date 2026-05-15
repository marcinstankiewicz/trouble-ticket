package service.troubleticket.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketCloseStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("status")
    private String status;
}

