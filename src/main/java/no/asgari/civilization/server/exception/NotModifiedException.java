package no.asgari.civilization.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_MODIFIED)
public class NotModifiedException extends RuntimeException {
}
