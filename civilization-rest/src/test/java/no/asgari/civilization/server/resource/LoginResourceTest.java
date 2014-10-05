package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import lombok.Cleanup;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoginResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";
    private List<NewCookie> cookies;

    @Test
    public void transformsCredentialsToPrincipals() throws Exception {
        DBCursor<Player> players = playerCollection.find(DBQuery.is("username", "cash1981"), new BasicDBObject());
        Player player = players.next();
        assertThat(player.getUsername()).isEqualToIgnoringCase("cash1981");
        assertThat(player.getPassword()).isEqualToIgnoringCase("0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33");
        ObjectMapper mapper = new ObjectMapper();
        String authEncoded = B64Code.encode("cash1981" + ":" + "foo", StringUtil.__ISO_8859_1);

        assertThat(client().resource(UriBuilder.fromPath(String.format(BASE_URL + "/login/secret", RULE.getLocalPort())).build())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + authEncoded)
                .entity(mapper.writeValueAsString(player))
                .type(MediaType.APPLICATION_JSON)
                .get(String.class))
                .isEqualTo("ack");
    }


    @Test
    public void shouldGet403WithWrongUsernamePass() {
        ClientResponse response = client().resource(
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
        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .queryParam("username", "cash1981")
                        .queryParam("password", "foo")
                        .build()
        )
                .type(MediaType.TEXT_PLAIN)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.getLocation().toASCIIString().matches(".*/player/.*/game.*"));

        cookies = response.getCookies();
        assertThat(cookies).isNotEmpty();
    }

    @Test
    public void createExistingPlayer() throws JsonProcessingException {
        Client client = Client.create();

        Player one = playerCollection.findOne();
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername(one.getUsername());
        playerDTO.setPassword(one.getPassword());
        playerDTO.setPasswordCopy(one.getPassword());
        playerDTO.setEmail(one.getEmail());

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        ClientResponse response = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/player", RULE.getLocalPort()))
                        .build()
        )
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        String message = response.getEntity(String.class);
        assertThat(message).isNotNull();
        assertThat(message).isEqualTo("Player already exists");
    }

    @Test
    public void createPlayer() throws JsonProcessingException {
        @Cleanup DBCursor<Player> foobar = playerCollection.find(DBQuery.is("username", "foobar"));
        if(foobar.hasNext()) {
            playerCollection.removeById(foobar.next().getId());
        }

        Client client = Client.create();

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername("foobar");
        playerDTO.setPassword("foobar");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player", RULE.getLocalPort())).build();
        ClientResponse response = client.resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        System.out.println(response.getLocation().toASCIIString());
        assertThat(response.getLocation().getPath()).contains(uri.getPath());
    }

    @Test
    public void deletePlayer() throws Exception {
        DBCursor<Player> foobar = playerCollection.find(DBQuery.is("username", "foobar"));
        if(!foobar.hasNext()) {
            createPlayer();
        }

        foobar = playerCollection.find(DBQuery.is("username", "foobar"));
        if(!foobar.hasNext()) {
            Assert.fail("Should have created foobar");
        }

        String pId = foobar.next().getId();
        Client client = Client.create();
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s", RULE.getLocalPort(), pId)).build();
        ClientResponse response = client.resource(uri)
                .delete(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void deleteUserThatDoesntExist() throws Exception {
        Client client = Client.create();
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s", RULE.getLocalPort(), "abc1234" )).build();
        ClientResponse response = client.resource(uri)
                .delete(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void checkWrongPasswordMatch() throws Exception {
        Client client = Client.create();

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername("foobar2");
        playerDTO.setPassword("foobar2");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player", RULE.getLocalPort())).build();
        ClientResponse response = client.resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

    @Test
    @Ignore("Must get validation to work before this can be passed")
    public void checkMissingUsername() throws Exception {
        Client client = Client.create();

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setPassword("foobar");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player", RULE.getLocalPort())).build();
        ClientResponse response = client.resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

}
