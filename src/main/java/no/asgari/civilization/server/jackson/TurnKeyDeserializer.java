package no.asgari.civilization.server.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.asgari.civilization.server.model.TurnKey;

import java.io.IOException;

public class TurnKeyDeserializer extends KeyDeserializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public TurnKey deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return mapper.readValue(key, TurnKey.class);
    }

}
