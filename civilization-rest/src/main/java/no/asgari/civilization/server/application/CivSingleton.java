package no.asgari.civilization.server.application;

import com.google.common.cache.LoadingCache;
import lombok.extern.log4j.Log4j;

/**
 * Application singleton
 */
@SuppressWarnings("unchecked")
@Log4j
//This class is a @Singleton
public final class CivSingleton {

    private static CivSingleton civSingleton = new CivSingleton();
    private LoadingCache<String, String> usernameCache;

    private CivSingleton() {
    }

    public static CivSingleton getInstance() {
        return civSingleton;
    }

    public void setUsernameCache(LoadingCache<String, String> usernameCache) {
        this.usernameCache = usernameCache;
    }

    public LoadingCache<String, String> getUsernameCache() {
        return usernameCache;
    }
}
