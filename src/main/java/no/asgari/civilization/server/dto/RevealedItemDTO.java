package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import no.asgari.civilization.server.model.Item;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevealedItemDTO implements Comparable<RevealedItemDTO> {
    private String username;
    private List<Item> items;

    @Override
    public int compareTo(RevealedItemDTO o) {
        return username.compareTo(o.getUsername());
    }
}
