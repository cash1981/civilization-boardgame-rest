package no.asgari.civilization.views;

import com.google.common.base.Charsets;
import com.mongodb.DBObject;
import io.dropwizard.views.View;

import java.util.List;

public class GameView extends View {

    private List<DBObject> PBFs;

    public GameView(List<DBObject> blogs) {
        super("/views/index.ftl", Charsets.UTF_8);
        this.PBFs = blogs;
    }
}
