package no.asgari.civilization.views;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;
import no.asgari.civilization.representations.PBF;

import java.util.List;

public class GameView extends View {

    private List<PBF> PBFs;

    public GameView(List<PBF> blogs) {
        super("/views/index.ftl", Charsets.UTF_8);
        this.PBFs = blogs;
    }
}
