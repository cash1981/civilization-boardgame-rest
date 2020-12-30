/*
 * Copyright (c) 2015-2021 Shervin Asgari
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("infantry")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used", "itemNumber"}, callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Infantry extends Unit implements Image {
    private int level = 0;
    private int attack;
    private int health;
    private String ownerId;
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private boolean isInBattle;
    private String image;
    private SheetName sheetName;
    private int itemNumber;

    public Infantry(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public SheetName getSheetName() {
        return sheetName = SheetName.INFANTRY;
    }

    @Override
    public String revealPublic() {
        switch (level) {
            case LEVEL_1:
                return "Spearmen";
            case LEVEL_2:
                return "Pikemen";
            case LEVEL_3:
                return "Riflemen";
            case LEVEL_4:
                return "Modern Infantry";
            default:
                return "Infantry";
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
                return "Spearmen " + attack + "." + health;
            case LEVEL_2:
                return "Pikemen " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Riflemen " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Modern Infantry " + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Infantry " + attack + "." + health;
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
