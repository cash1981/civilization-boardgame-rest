package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@JsonRootName("playerDTO")
@Data
public class PlayerDTO {
    @NotNull
    private String username;

    @Email
    @NotNull
    private String email;

    @NotBlank
    @NotNull
    private String password;

    @NotBlank
    @NotNull
    private String passwordCopy;

    private String pbfId;

}
