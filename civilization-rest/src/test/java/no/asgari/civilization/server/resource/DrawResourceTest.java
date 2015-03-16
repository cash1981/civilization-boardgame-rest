package no.asgari.civilization.server.resource;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

@Ignore
public class DrawResourceTest extends MongoDBBaseTest {

    @ClassRule
    public static final DropwizardAppRule<CivilizationConfiguration> RULE =
            new DropwizardAppRule<CivilizationConfiguration>(CivilizationApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";

    @Test
    public void drawUnitsForBattle() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        DBCursor<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId));
        int count = gameLogs.count();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", RULE.getLocalPort(), pbfId)).build();
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
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/end", RULE.getLocalPort(), pbfId)).build();
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

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle", RULE.getLocalPort(), pbfId)).build();
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

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/battle/draw", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .queryParam("numOfUnits", "foobar")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    private void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/", RULE.getLocalPort(), pbfId, SheetName.ARTILLERY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    private void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s/", RULE.getLocalPort(), pbfId, SheetName.INFANTRY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

}
