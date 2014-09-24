package no.asgari.civilization.server.eventbus;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

/**
 * Class that will handle all the event publishing
 */
@RequiredArgsConstructor
public class EventPublisher {

    private final EventBus eventBus;

}
