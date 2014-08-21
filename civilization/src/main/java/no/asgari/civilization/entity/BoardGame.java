package no.asgari.civilization.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BoardGame implements Owner {
    protected List<Item> items = new ArrayList<>();

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

}
