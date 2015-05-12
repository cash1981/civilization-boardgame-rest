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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonRootName("player")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    @JsonIgnore
    public static final String COL_NAME = "player";
    @JsonIgnore
    public static final String USERNAME = "username";
    @JsonIgnore
    public static final String EMAIL = "email";

    @ObjectId
    @Id
    private String id;

    @NotBlank
    //Unique
    private String username;

    @Email
    private String email;

    private boolean disableEmail = false;

    @NotBlank
    private String password;

    /**
     * Set of unique active games This may be reduntant as it can be calculated by looping through all pbfs.players and finding match.
     */
    private Set<String> gameIds = new HashSet<>();
}
