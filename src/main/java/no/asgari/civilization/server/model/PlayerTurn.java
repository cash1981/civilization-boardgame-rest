package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PlayerTurn {
    private int turnNumber = 1;
    private String username;
    private Map<String, Boolean> setupMap = new HashMap<>(1);
    private Map<String, Boolean> sotMap = new HashMap<>(1);
    private Map<String, Boolean> tradeMap = new HashMap<>(1);
    private Map<String, Boolean> cmMap = new HashMap<>(1);
    private Map<String, Boolean> movementMap = new HashMap<>(1);
    private Map<String, Boolean> researchMap = new HashMap<>(1);

    /**
     * If all orders are finished, turn is ended
     */
    @JsonIgnore
    public boolean endTurn() {
        if(turnNumber == 1) {
            if(setupMap.containsValue(true)) {
                turnNumber++;
            }
            return false;
        }

        if(sotMap.containsValue(true) && tradeMap.containsValue(true)
                && cmMap.containsValue(true) && movementMap.containsValue(true) && researchMap.containsValue(true)) {
            turnNumber++;
            return true;
        }
        return false;
    }

    public String getSetup() {
        Set<String> strings = setupMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }

    public String getSot() {
        Set<String> strings = sotMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }

    public String getTrade() {
        Set<String> strings = tradeMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }

    public String getCm() {
        Set<String> strings = cmMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }

    public String getMovement() {
        Set<String> strings = movementMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }

    public String getResearch() {
        Set<String> strings = researchMap.keySet();
        if(!strings.isEmpty()) {
            return strings.iterator().next();
        }

        return null;
    }
}
