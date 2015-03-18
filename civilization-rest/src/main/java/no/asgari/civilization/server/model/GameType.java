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
