package service.troubleticket.rs.v1.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Pole text ma niedozwoloną wartość dla tej operacji.")
    private String text;
}
