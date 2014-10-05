package no.asgari.civilization.server.application;

import com.google.common.eventbus.EventBus;
import lombok.extern.log4j.Log4j;

/**
 * Application cache
 */
@SuppressWarnings("unchecked")
@Log4j
//This class is a @Singleton
public final class CivSingleton {

    private static CivSingleton civSingleton = new CivSingleton();
    private static EventBus eventBus = new EventBus();

    private CivSingleton() {
    }

    public static CivSingleton getInstance() {
        return civSingleton;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void postToEventBus(Object event) {
        eventBus.post(event);
    }
}
