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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString()
@JsonTypeName("wonder")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used", "itemNumber"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wonder implements Item {
    @JsonIgnore
    public static final String ANCIENT = "Ancient";
    @JsonIgnore
    public static final String MEDIEVAL = "Medieval";
    @JsonIgnore
    public static final String MODERN = "Modern";
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden = true;
    private String ownerId;
    private SheetName sheetName;
    private int itemNumber;

    public Wonder(String name, String description, String type, SheetName sheetName) {
        this.name = name;
        this.description = description;
        this.sheetName = sheetName;
        this.type = type;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public String revealPublic() {
        return name;
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
