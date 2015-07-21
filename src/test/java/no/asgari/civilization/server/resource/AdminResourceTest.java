package no.asgari.civilization.server.resource;

import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/api", RULE.getLocalPort());

    @Test
    public void adminCanChangeUser() {
        PBF pbf = getApp().pbfCollection.findOne();

        Response response = client().target(UriBuilder.fromPath(BASE_URL + "/admin/changeuser").build())
                .queryParam("gameid", pbf.getId())
                .queryParam("fromUsername", "Itchi")
                .queryParam("toUsername", "DaveLuca")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getAdminEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);


        pbf = getApp().pbfCollection.findOne();
        long count = pbf.getPlayers().stream().filter(p -> p.getUsername().equals("DaveLuca")).count();
        assertThat(count).isEqualTo(1L);
    }
}
