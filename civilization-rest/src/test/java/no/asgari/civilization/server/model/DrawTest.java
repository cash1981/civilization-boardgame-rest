package no.asgari.civilization.server.model;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DrawTest extends AbstractMongoDBTest {

    @Test
    public void drawCivAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);
        Draw<Civ> draw = drawAction.drawCiv(pbfId, playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(draw.getPbfId()).isEqualTo(pbfId);
        assertThat(pbf.getCivs()).doesNotContain(draw.getItem());
    }

    @Test
    public void drawAircraftAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);
        //Before draw
        int aircrafts = pbfCollection.findOneById(pbfId).getAircraft().size();

        Draw<Aircraft> draw = drawAction.drawAircraft(pbfId, playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(draw.getPbfId()).isEqualTo(pbfId);
        assertThat(pbf.getAircraft().size()).isLessThan(aircrafts);
        assertThat(draw.getItem()).isExactlyInstanceOf(Aircraft.class);
    }

    @Test
    public void drawArtilleryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);
        //Before draw
        int artilleries = pbfCollection.findOneById(pbfId).getArtillery().size();
        Draw<Artillery> draw = drawAction.drawArtillery(pbfId, playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(draw.getPbfId()).isEqualTo(pbfId);
        assertThat(draw.getItem()).isExactlyInstanceOf(Artillery.class);
        assertThat(pbf.getArtillery().size()).isLessThan(artilleries);
    }

    @Test
    public void drawCitystateAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);
        Draw<Citystate> draw = drawAction.drawCitystate(pbfId, playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(draw.getPbfId()).isEqualTo(pbfId);
        assertThat(pbf.getCitystates()).doesNotContain(draw.getItem());
    }

}
