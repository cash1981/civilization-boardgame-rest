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

/**
 * The item you pull for instance Great Person, Wonder, Civ etc
 * Most of them will not have type or description.
 * The @used attribute will determine if the item is available or not
 * ie:
 * <p>
 * sheet = GREAT_PERSON
 * name = Leonidas
 * type = General
 * description = Battle: Each time you start a battle with fewer units in your battle force than your opponent,
 * your combat bonus is increased by 8 until the end of the battle.
 * used = true
 * hidden = false
 * <p>
 * sheet = CIV
 * name = America
 * type = null
 * description = null
 * used = false
 * hidden = false;
 * <p>
 * sheet = VILLAGES
 * name = Barbarian Encampment
 * type = null
 * description = Start of Turn : Choose an empty square not in the outskirts of a city. Place a village token on that square.
 * used = true
 * hidden = true;
 */
public interface Item extends Spreadsheet, Type, Comparable<Spreadsheet> {

    /**
     * Either the username or pbf name, both of which must be unique *
     */
    public String getName();

    public String getOwnerId();

    public void setOwnerId(String owner);

    public boolean isHidden();

    public void setHidden(boolean hidden);

    public boolean isUsed();

    public String getDescription();

    public int getItemNumber();

    public void setItemNumber(int itemNumber);

}
