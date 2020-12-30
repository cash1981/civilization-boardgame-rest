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

package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@JsonRootName("itemDTO")
@Setter
@Getter
@ToString(of = "name")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {
    private int itemNumber;
    /**
     * ie: Leonidas *
     */
    @NotEmpty
    private String name;
    /**
     * if it is to be sent to a new playerId
     */
    private String ownerId;
    /**
     * If the item is to be revealed
     */
    private boolean hidden;
    private boolean used;
    private String description;
    /**
     * Type of item, ie Scientist
     */
    private String type;
    /**
     * ie Great Person
     */
    @NotNull
    private String sheetName;

    private String pbfId;
}
