package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoginResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d/civilization";

    private Player loggedInPlayer;

    @Test
    public void shouldGet403WithWrongUsernamePass() {
        Client client = Client.create();

        ClientResponse response = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .queryParam("username", "cash1981")
                        .queryParam("password", "fifafoo")
                        .build()
        )
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void shouldLoginCorrectly() {
        Client client = Client.create();
        ClientResponse response = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .queryParam("username", "cash1981")
                        .queryParam("password", "foo")
                        .build()
        )
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.getLocation().toASCIIString().contains("games"));

        Player p = response.getEntity(Player.class);
        assertThat(p).isNotNull();
        loggedInPlayer = p;
    }

    @Test
    @Ignore
    public void shouldGetAccessDenied() {
        Client client = Client.create();

        ClientResponse response = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login/test", RULE.getLocalPort()))
                        .build()
        ).entity(new Player())
            .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    }

    @Test
    @Ignore
    public void shouldBeAbleToCallProtectedMethod() throws JsonProcessingException {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE);
        Client client = Client.create(clientConfig);

        if(loggedInPlayer == null) {
            shouldLoginCorrectly();
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(loggedInPlayer);
        System.out.println(json);

        ClientResponse response =
                client.resource(
                        UriBuilder.fromPath(String.format(BASE_URL + "/login/test", RULE.getLocalPort()))
                                .build()
                ).entity(json)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }
}
