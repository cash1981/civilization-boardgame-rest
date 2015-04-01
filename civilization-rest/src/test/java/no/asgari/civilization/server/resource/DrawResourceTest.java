package no.asgari.civilization.server.resource;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Village;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DrawResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/civilization", RULE.getLocalPort());

    @Test
    public void drawUnitsForBattleThenDiscard() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", getApp().pbfId)).build();
        Response response = client()
                .target(uri)
                .queryParam("numOfUnits", "3")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        List list = response.readEntity(List.class);
        assertThat(list).hasSize(3);

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battlehand/reveal", getApp().pbfId)).build();
        response = client()
                .target(uri)
                .queryParam("numOfUnits", "3")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());

    }

    @Test
    public void drawUnitsWithNoParam() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", getApp().pbfId)).build();
        Response response = client()
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        List list = response.readEntity(List.class);
        assertThat(list).hasSize(0);
    }

    @Test
    public void drawUnitsWithInvalidParamShouldResult404() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s", getApp().pbfId)).build();
        Response response = client()
                .target(uri)
                .queryParam("numOfUnits", "foobar")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(null);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testLooting() throws Exception {
        drawVillage();

        String pid = getAnotherPlayerId();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/loot/%s", getApp().pbfId, SheetName.VILLAGES, pid)).build();
        Response response = client()
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void testThatYouCannotLootInvalidItem() throws Exception {
        testDrawArtilleryCard();

        String pid = getAnotherPlayerId();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/loot/%s", getApp().pbfId, SheetName.ARTILLERY, pid)).build();
        Response response = client()
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE_406);
    }

    @Test
    public void testThatYouCannotDrawInvalidSheet() throws Exception {
        testDrawArtilleryCard();

        String pid = getAnotherPlayerId();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/loot/%s", getApp().pbfId, "foobar", pid)).build();
        Response response = client()
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void drawBarariansThenDiscard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle/barbarians", getApp().pbfId)).build();
        Response response = client()
                .target(uri)
                .queryParam("numOfUnits", "3")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        List list = response.readEntity(List.class);
        assertThat(list).hasSize(3);

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle/discard/barbarians", getApp().pbfId)).build();
        response = client()
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
    }

    private String getAnotherPlayerId() {
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        String pid = "";
        for(Playerhand pl : pbf.getPlayers()) {
            if(!pl.getPlayerId().equals(getApp().playerId)) {
                pid = pl.getPlayerId();
                break;
            }
        }
        return pid;
    }

    private void drawVillage() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.VILLAGES);
        assertTrue(gameLogOptional.isPresent());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Village.class);
    }

    private void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.ARTILLERY)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    private void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.INFANTRY)).build();

        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }
}
