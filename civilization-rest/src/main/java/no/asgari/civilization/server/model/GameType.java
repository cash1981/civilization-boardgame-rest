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

import java.util.Optional;
import java.util.stream.Stream;

public enum GameType {
    BASE("Base Game"), FAF("Fame and Fortune"), WAW("Wisdom and Warfare"), DOC("Dawn of Civilization");

    private String label;

    GameType(String name) {
        this.label = name;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Optional<GameType> find(String name) {
        String spacesRemovedName = name.replaceAll("\\s", "");
        Optional<GameType> found = Stream.of(BASE, FAF, WAW, DOC)
                .filter(type -> type.label.replaceAll("\\s", "").equalsIgnoreCase(spacesRemovedName))
                .findFirst();
        if (!found.isPresent()) {
            try {
                return Optional.of(valueOf(name.toUpperCase()));
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
        return found;
    }

}
