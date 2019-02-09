package no.asgari.civilization.server.repository;

import no.asgari.civilization.server.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    List<Chat> findAllByPbfIdOrderByCreated(String pbfId);

    List<Chat> findTop50ByPbfIdIsNullOrderByCreated();
}
