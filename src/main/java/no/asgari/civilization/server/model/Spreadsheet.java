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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.asgari.civilization.server.SheetName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Citystate.class, name = "citystate"),
        @JsonSubTypes.Type(value = Civ.class, name = "civ"),
        @JsonSubTypes.Type(value = CultureI.class, name = "cultureI"),
        @JsonSubTypes.Type(value = CultureII.class, name = "cultureII"),
        @JsonSubTypes.Type(value = CultureIII.class, name = "cultureIII"),
        @JsonSubTypes.Type(value = GreatPerson.class, name = "greatperson"),
        @JsonSubTypes.Type(value = Hut.class, name = "hut"),
        @JsonSubTypes.Type(value = Tile.class, name = "tile"),
        @JsonSubTypes.Type(value = Village.class, name = "village"),
        @JsonSubTypes.Type(value = Wonder.class, name = "wonder"),
        @JsonSubTypes.Type(value = Infantry.class, name = "infantry"),
        @JsonSubTypes.Type(value = Mounted.class, name = "mounted"),
        @JsonSubTypes.Type(value = Artillery.class, name = "artillery"),
        @JsonSubTypes.Type(value = Aircraft.class, name = "aircraft"),
        @JsonSubTypes.Type(value = Tech.class, name = "tech"),
        @JsonSubTypes.Type(value = SocialPolicy.class, name = "socialpolicy")
})
public interface Spreadsheet {

    public abstract SheetName getSheetName();

    /**
     * Is used to reveal hidden public information about the item. Will not reveal the content, just the type*
     */
    @JsonIgnore
    public abstract String revealPublic();

    @JsonIgnore
    /** Is used to hidden all information about the item. **/
    public abstract String revealAll();
}
