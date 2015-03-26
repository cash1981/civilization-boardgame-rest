package no.asgari.civilization.server.misc;

import no.asgari.civilization.server.model.PBF;

/**
 * Some common security checks which each action should perform
 */
public class SecurityCheck {

    public static boolean hasUserAccess(PBF pbf, String playerId) {
        if (pbf != null) {
            return pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId));
        }

        return false;
    }
}
