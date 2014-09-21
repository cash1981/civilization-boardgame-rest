package no.asgari.civilization.server.model;

import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;
import org.mongojack.WriteResult;

import static org.fest.assertions.api.Assertions.assertThat;

public class DrawTest extends AbstractMongoDBTest {

    @Test
    public void drawCivAndUpdateCollectionByRemovingTheDraw() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        int initialSizeOfCiv = pbf.getCivs().size();

        Civ civ = pbf.getCivs().remove(0);
        assertThat(civ).isNotNull();

        Draw<Civ> draw = new Draw<>(pbfId, playerId);
        draw.setItem(civ);
        WriteResult<Draw, String> drawInsert = drawCollection.insert(draw);
        assertThat(drawCollection.findOneById(drawInsert.getSavedId())).isNotNull();

        //update pdf
        WriteResult<PBF, String> updateResult = pbfCollection.updateById(pbf.getId(), pbf);
        System.out.println(updateResult);

        assertThat(pbfCollection.findOneById(pbf.getId()).getCivs().size()).isEqualTo(initialSizeOfCiv-1);
    }

}
