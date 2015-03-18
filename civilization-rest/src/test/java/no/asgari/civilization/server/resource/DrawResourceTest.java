package no.asgari.civilization.server.resource;

import static junit.framework.Assert.assertEquals;

import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.mongodb.MongoDBTest;
import org.junit.ClassRule;
import org.junit.Ignore;

@Ignore
public class DrawResourceTest extends MongoDBTest {
    protected static String BASE_URL = String.format("http://localhost:%d/civilization", RULE.getLocalPort());

    /*
    @Test
    public void drawUnitsForBattle() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        DBCursor<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", getApp().pbfId));
        int count = gameLogs.count();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", RULE.getLocalPort(), getApp().pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .queryParam("numOfUnits", "3")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        List list = response.getEntity(List.class);
        assertThat(list).hasSize(3);

        //finally end battle
        endBattle();
    }

    private void endBattle() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/end", RULE.getLocalPort(), getApp().pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void drawUnitsWithNoParam() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", RULE.getLocalPort(), getApp().pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        List list = response.getEntity(List.class);
        assertThat(list).hasSize(0);
    }

    @Test
    public void drawUnitsWithInvalidParamShouldResult404() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle/draw", RULE.getLocalPort(), getApp().pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .queryParam("numOfUnits", "foobar")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    private void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/", RULE.getLocalPort(), getApp().pbfId, SheetName.ARTILLERY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    private void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/", RULE.getLocalPort(), getApp().pbfId, SheetName.INFANTRY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }
*/
}
