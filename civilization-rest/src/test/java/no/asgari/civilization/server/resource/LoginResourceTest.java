package no.asgari.civilization.server.resource;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.application.SimpleAuthenticator;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoginResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";
    private List<NewCookie> cookies;

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        final Authenticator<BasicCredentials, Player> authenticator = new SimpleAuthenticator(playerCollection);
        config.getSingletons().add(new BasicAuthProvider<>(authenticator, "civilization"));
        config.getSingletons().add(new LoginResource(playerCollection));
        return new LowLevelAppDescriptor.Builder(config).build();
    }

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

}
