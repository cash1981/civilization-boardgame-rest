package no.asgari.civilization.server.repository;

import no.asgari.civilization.server.model.GameLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameLogRepository extends MongoRepository<GameLog, String> {

    List<GameLog> findAllByPbfId(String pbfId);
    List<GameLog> findAllByPbfIdAndUsername(String pbfId, String username);
}
