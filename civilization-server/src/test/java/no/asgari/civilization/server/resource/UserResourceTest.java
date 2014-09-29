package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;

import static org.fest.assertions.api.Assertions.assertThat;

public class UserResourceTest extends AbstractMongoDBTest {

    //@ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d/civilization";

    @Test
    public void createUserTest() throws JsonProcessingException {
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
                        UriBuilder.fromPath(String.format(BASE_URL + "/user", RULE.getLocalPort()))
                            .build()
                        )
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        //Player p = response.getEntity(Player.class);
        //assertThat(p).isNotNull();
    }

}
