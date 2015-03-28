package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageDTO {

        private final String message;

        @JsonCreator
        public MessageDTO(@JsonProperty("message") String message) {
            this.message = message;
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

}
