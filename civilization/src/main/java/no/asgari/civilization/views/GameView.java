package no.asgari.civilization.views;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;
import no.asgari.civilization.representations.Game;

import java.util.List;

public class GameView extends View {

    private List<Game> games;

    public GameView(List<Game> blogs) {
        super("/views/index.ftl", Charsets.UTF_8);
        this.games = blogs;
    }
}
