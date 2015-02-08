package no.asgari.civilization.server.model;

/**
 * Most Items have images associated with them
 */
@FunctionalInterface
public interface Image {
    final String PNG = ".png";
    String getImage();
}
