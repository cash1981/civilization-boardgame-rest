package no.asgari.civilization.server.application;

import com.google.common.cache.Cache;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Player;

import java.util.UUID;

/**
 * Application cache
 */
@Log4j
@SuppressWarnings("unchecked")
public final class CivCache {

    private static Cache<String, UUID> tokenCache;
    private static CivCache civCache = new CivCache();

    private CivCache() {
    }


    static CivCache getInstance(Cache<String, UUID> tokenCache) {
        if(CivCache.tokenCache != null) {
            return civCache;
        }

        CivCache.tokenCache = tokenCache;
        return civCache;
    }

    public static CivCache getInstance() {
        return civCache;
    }

    /**
     * Put players token in cache
     * Uses the username as key
     * @param player
     */
    public void put(Player player) {
        log.debug("Putting player " + player.getUsername() + " in cache");
        tokenCache.put(player.getUsername(), player.getToken());
    }

    public boolean findUser(String username, String token) {
        UUID uuid = tokenCache.getIfPresent(username);
        if(uuid == null) return false;

        return token.equals(uuid.toString());
    }

    /**
     * Key is username and UUID is value
     * @return
     */
    public Cache<String, UUID> getCache() {
        return tokenCache;
    }
}
