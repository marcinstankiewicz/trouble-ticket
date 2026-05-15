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

    @NotBlank
    private String externalId;

    @NotNull
    private Long serviceId;

    @NotBlank
    private String description;

    @NotBlank
    private String status;

    @NotBlank
    private String note;
}
