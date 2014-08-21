package no.asgari.civilization.db;

import com.google.common.base.Optional;
import io.dropwizard.hibernate.AbstractDAO;
import no.asgari.civilization.entity.Player;
import org.hibernate.SessionFactory;

import java.util.List;

public class PlayerDAO extends AbstractDAO<Player> {
    public PlayerDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<Player> findById(Long id) {
        return Optional.fromNullable(get(id));
    }

    public Player create(Player person) {
        return persist(person);
    }

    public List<Player> findAll() {
        return list(namedQuery("no.asgari.civilization.entity.Player.findAll"));
    }
}
