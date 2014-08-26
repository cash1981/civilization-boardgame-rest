package no.asgari.civilization.representations;

import com.google.common.collect.Lists;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

public class Player {

    @Id
    @ObjectId
    private String id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private List<Item> items = Lists.newArrayList();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
