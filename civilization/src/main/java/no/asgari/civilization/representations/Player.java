package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(value="_id")
public class Player {
    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    private List<Item> items = Lists.newArrayList();
    private List<Unit> units = Lists.newArrayList();
    private List<Integer> gameIds = Lists.newArrayList();
}
