package no.asgari.civilization.server.resource;

import no.asgari.civilization.server.SheetName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@Ignore
public class DrawResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/civilization", RULE.getLocalPort());

    @Test
    public void drawUnitsForBattle() throws Exception {
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

    }

    @Test
    public void drawUnitsWithNoParam() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s", getApp().pbfId)).build();
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
