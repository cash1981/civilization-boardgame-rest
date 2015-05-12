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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;

@Setter
public abstract class Unit implements Item, Level {
    String type;
    String name;

    @JsonIgnore
    static final int LEVEL_1 = 1;
    @JsonIgnore
    static final int LEVEL_2 = 2;
    @JsonIgnore
    static final int LEVEL_3 = 3;
    @JsonIgnore
    static final int LEVEL_4 = 4;

    public abstract boolean isKilled();

    public abstract int getAttack();

    public abstract int getHealth();

    @Override
    public String getName() {
        return name = getType() + " " + getAttack() + "." + getHealth();
    }

    @JsonIgnore
    @Override
    public String getDescription() {
        return getType();
    }

    @Override
    public String revealPublic() {
        return getType();
    }

    @Override
    public String revealAll() {
        return toString();
    }

    public abstract boolean isInBattle();

    public abstract void setInBattle(boolean inBattle);
}
