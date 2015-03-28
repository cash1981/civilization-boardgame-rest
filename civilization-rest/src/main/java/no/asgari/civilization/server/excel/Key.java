/*
 * Copyright (c) 2015 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
