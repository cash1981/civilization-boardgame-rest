package no.asgari.civilization.server.resource;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilderSpec;
import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.CachingAuthenticator;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.testing.junit.DropwizardAppRule;
import lombok.Cleanup;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoginResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivilizationConfiguration> RULE =
            new DropwizardAppRule<>(CivilizationApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {
        @GET
        public String show(@Auth Player principal) {
            return "ack";
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        //final Authenticator<BasicCredentials, Player> authenticator = new CivAuthenticator(playerCollection);
        final CachingAuthenticator<BasicCredentials, Player> authenticator =
        new CachingAuthenticator<>(new MetricRegistry(), new CivAuthenticator(db),
                CacheBuilderSpec.parse("maximumSize=1"));

        config.getSingletons().add(new BasicAuthProvider<>(authenticator, "civilization"));
        config.getSingletons().add(new ExampleResource());
        config.getSingletons().add(new LoginResource(db));

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void transformsCredentialsToPrincipals() throws Exception {
        DBCursor<Player> players = playerCollection.find(DBQuery.is("username", "cash1981"), new BasicDBObject());
        Player player = players.next();
        ObjectMapper mapper = new ObjectMapper();

        assertThat(client().resource("/test/")
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(mapper.writeValueAsString(player))
                .type(MediaType.APPLICATION_JSON)
                .get(String.class))
                .isEqualTo("ack");
    }

    @Test
    public void shouldGet403WithWrongUsernamePass() {
        Form form = new Form();
        form.add("username", "cash1981");
        form.add("password", "fifafoo");

        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .build()
        )
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(ClientResponse.class, form);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void shouldLoginCorrectly() {
        Form form = new Form();
        form.add("username", "cash1981");
        form.add("password", "foo");

        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .build()
        )
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(ClientResponse.class, form);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.getLocation().toASCIIString().matches(".*/player/.*/game.*"));
    }

    @Test
    public void createExistingPlayer() throws JsonProcessingException {
        Player one = playerCollection.findOne();
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername(one.getUsername());
        playerDTO.setPassword(one.getPassword());
        playerDTO.setPasswordCopy(one.getPassword());
        playerDTO.setEmail(one.getEmail());

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort()))
                        .build()
        )
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
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

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername("foobar");
        playerDTO.setPassword("foobar");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
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
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/login/%s", RULE.getLocalPort(), pId)).build();
        ClientResponse response = client().resource(uri)
                .delete(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void deleteUserThatDoesntExist() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/login/%s", RULE.getLocalPort(), "abc1234" )).build();
        ClientResponse response = client().resource(uri)
                .delete(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void checkWrongPasswordMatch() throws Exception {
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setUsername("foobar2");
        playerDTO.setPassword("foobar2");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        assertThat(response.getEntity(String.class)).isEqualTo("Passwords are not identical");
    }

    @Test
    @Ignore("This will work in integration test, but not using JerseyTest")
    public void checkMissingUsername() throws Exception {
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setPassword("foobar");
        playerDTO.setPasswordCopy("foobar");
        playerDTO.setEmail("foobar@mailinator.com");

        ObjectMapper mapper = new ObjectMapper();
        String playerDtoJson = mapper.writeValueAsString(playerDTO);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/login", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                //.header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .type(MediaType.APPLICATION_JSON)
                .entity(playerDtoJson)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

}
