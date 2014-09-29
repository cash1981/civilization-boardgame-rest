package no.asgari.civilization.server.application;

import com.google.common.cache.Cache;
import lombok.extern.log4j.Log4j;

import java.util.UUID;

@Log4j
@SuppressWarnings("unchecked")
public class CivCache {

    private static Cache<String, UUID> tokenCache;
    private static CivCache cache = new CivCache();

    private CivCache() {
    }


    public static CivCache getInstance(Cache<String, UUID> tokenCache) {
        if(CivCache.tokenCache != null) {
            return cache;
        }

        CivCache.tokenCache = tokenCache;
        return cache;
    }

    public static CivCache getInstance() {
        return cache;
    }

    /**
     * Key is username and UUID is value
     * @return
     */
    public static Cache<String, UUID> getTokenCache() {
        return tokenCache;
    }
}
