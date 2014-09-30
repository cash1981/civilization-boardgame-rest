package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import lombok.Cleanup;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.fest.assertions.api.Assertions.assertThat;

public class PlayerResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d/civilization";

    @Test
    public void createExistingUser() throws JsonProcessingException {
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
        PlayerExistException ex = response.getEntity(PlayerExistException.class);
        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("Player already exists");
    }

    @Test
    public void createUser() throws JsonProcessingException {
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
    public void deleteUser() throws Exception {
        DBCursor<Player> foobar = playerCollection.find(DBQuery.is("username", "foobar"));
        if(!foobar.hasNext()) {
            createUser();
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
