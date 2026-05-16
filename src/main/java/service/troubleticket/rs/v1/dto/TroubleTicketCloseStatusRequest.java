package service.troubleticket.rs.v1.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketCloseStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Pole status ma niedozwoloną wartość dla tej operacji.")
    private String status;
}
