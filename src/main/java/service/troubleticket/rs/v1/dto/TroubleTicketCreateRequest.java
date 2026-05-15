package service.troubleticket.rs.v1.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroubleTicketCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Pole externalId ma niedozwoloną wartość dla tej operacji.")
    private String externalId;

    @NotNull(message = "Pole serviceId ma niedozwoloną wartość dla tej operacji.")
    private Long serviceId;

    @NotBlank(message = "Pole description ma niedozwoloną wartość dla tej operacji.")
    private String description;

    @NotBlank(message = "Pole status ma niedozwoloną wartość dla tej operacji.")
    private String status;

    @NotBlank(message = "Pole note ma niedozwoloną wartość dla tej operacji.")
    private String note;
}
