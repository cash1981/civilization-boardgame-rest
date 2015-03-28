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

package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("mounted")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"}, callSuper = false)
public class Mounted extends Unit implements Image {
    private int level = 0;
    private String ownerId;
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;
    private boolean isInBattle;
    private String image;
    private SheetName sheetName;

    public Mounted(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public String getType() {
        type = getClass().getSimpleName();
        return type;
    }

    @Override
    public SheetName getSheetName() {
        return sheetName = SheetName.MOUNTED;
    }

    @Override
    public String revealPublic() {
        switch (level) {
            case LEVEL_1:
                return "Horsemen";
            case LEVEL_2:
                return "Knight";
            case LEVEL_3:
                return "Cavalry";
            case LEVEL_4:
                return "Tank";
            default:
                return "Mounted";
        }
    }

    @Override
    public String revealAll() {
        return toString();
    }

    @Override
    public String toString() {
        switch (level) {
            case LEVEL_1:
                return "Horsemen " + attack + "." + health;
            case LEVEL_2:
                return "Knight " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Cavalry " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Tank " + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Mounted " + attack + "." + health;
        }
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @JsonGetter("image")
    @Override
    public String getImage() {
        image = getType() + attack + "." + health + PNG;
        return image.replaceAll(" ", "");
    }
}
