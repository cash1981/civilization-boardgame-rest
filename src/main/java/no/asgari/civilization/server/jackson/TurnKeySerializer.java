package no.asgari.civilization.server.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import no.asgari.civilization.server.model.TurnKey;

import java.io.IOException;
import java.io.StringWriter;

public class TurnKeySerializer extends JsonSerializer<TurnKey> {
    @Override
    public void serialize(TurnKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (null == value) {
            throw new IOException("Could not serialize object to json, input object to serialize is null");
        }
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, value);
        gen.writeFieldName(writer.toString());
    }
}
