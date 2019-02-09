package no.asgari.civilization.server.repository;

import no.asgari.civilization.server.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {

    Optional<Player> findOneByUsername(String username);
    Optional<Player> findOneByEmail(String email);
}
