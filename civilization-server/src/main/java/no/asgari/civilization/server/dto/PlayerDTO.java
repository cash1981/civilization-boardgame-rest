package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@JsonRootName("playerDTO")
@Data
public class PlayerDTO {
    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordCopy;

    private String pbfId;

}
