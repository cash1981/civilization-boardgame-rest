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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Each PBF has a playerhand consisting of the player and its items
 */
@Data
@JsonRootName("players")
@NoArgsConstructor
@EqualsAndHashCode(of = {"username", "playerId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playerhand {
    @NotBlank
    //Can consider using the playerId instead or removing @NotBlank
    private String username;

    @NotBlank
    private String playerId;

    private String email;

    private String color;

    private Civ civilization; //The chosen civilization

    //Only one starting player each turn
    /**
     * Determines whos turn it is each round *
     */
    private boolean yourTurn = false;
    private boolean gameCreator = false;
    private boolean yourTurnEmailSent = false;

    private List<Item> items = new ArrayList<>();
    private Set<Tech> techsChosen = new TreeSet<>();
    private List<Unit> barbarians = new ArrayList<>(3);
    private List<Unit> battlehand = new ArrayList<>();

    @JsonIgnore
    public static String green() {
        return "Green";
    }

    @JsonIgnore
    public static String yellow() {
        return "Yellow";
    }

    @JsonIgnore
    public static String purple() {
        return "Purple";
    }

    @JsonIgnore
    public static String red() {
        return "Red";
    }

    @JsonIgnore
    public static String blue() {
        return "Blue";
    }
}
