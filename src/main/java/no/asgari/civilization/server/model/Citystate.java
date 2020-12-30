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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@ToString(of = "name")
@Getter
@Setter
@JsonTypeName("citystate")
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "type", "description"}, callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Citystate implements Item, Image {
    @JsonProperty
    @NotEmpty
    private String name;

    @JsonProperty
    private String type;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean used;
    @JsonProperty
    private boolean hidden = true;
    @JsonProperty
    private String ownerId; // (playerId)
    private String image;
    private SheetName sheetName;
    private int itemNumber;

    public Citystate(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return sheetName = SheetName.CITY_STATES;
    }

    @Override
    public String revealPublic() {
        return "City state: " + name;
    }

    @Override
    public String revealAll() {
        return revealPublic();
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @Override
    public String getImage() {
        image = description + PNG;
        return image.replaceAll(" ", "");
    }
}
