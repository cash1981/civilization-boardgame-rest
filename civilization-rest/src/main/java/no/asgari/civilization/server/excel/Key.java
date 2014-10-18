package no.asgari.civilization.server.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.GameType;

/**
 * To be used in CacheLoader as key
 */
@Data
@NoArgsConstructor
public class Key {

    private GameType gameType;
    private SheetName sheetName;

    public Key(GameType gameType, SheetName sheetName) {
        this.gameType = gameType;
        this.sheetName = sheetName;
    }
}
