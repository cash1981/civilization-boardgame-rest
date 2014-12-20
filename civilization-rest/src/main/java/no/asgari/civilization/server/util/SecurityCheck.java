package no.asgari.civilization.server.util;

import no.asgari.civilization.server.model.PBF;

/**
 * Some common security checks which each action should perform
 */
public class SecurityCheck {

    public static boolean hasUserAcces(PBF pbf, String playerId) {
        if(pbf != null) {
            return pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId));
        }

        return false;
    }
}
