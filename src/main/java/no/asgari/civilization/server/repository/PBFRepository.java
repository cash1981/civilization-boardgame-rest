package no.asgari.civilization.server.repository;

import no.asgari.civilization.server.model.PBF;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PBFRepository extends MongoRepository<PBF, String> {

    List<PBF> findAllByActiveFalseAndWinnerIsNull();
}
