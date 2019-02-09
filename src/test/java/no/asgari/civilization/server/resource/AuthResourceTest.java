package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Cleanup;
import no.asgari.civilization.server.dto.ForgotpassDTO;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/api", RULE.getLocalPort());

    @Test
    public void shouldGet403WithWrongUsernamePass() {
        Form form = new Form("username", "cash1981");
        form.param("password", "fifafoo");

        Response response = client().target(UriBuilder.fromPath(BASE_URL + "/auth/login").build())
                .request()
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void shouldLoginCorrectly() {
        Form form = new Form();
        form.param("username", "cash1981").param("password", "foo");

        Response response = client().target(BASE_URL + "/auth/login")
                .request()
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void createExistingPlayer() throws JsonProcessingException {
        Player one = getApp().playerRepository.findOne();
        Form form = new Form();
        form.param("username", one.getUsername());
        form.param("password", one.getPassword());
        form.param("email", one.getEmail());

        Response response = client().target(
                UriBuilder.fromPath(BASE_URL + "/auth/register").build())
                .request()
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void createPlayer() throws JsonProcessingException {
        @Cleanup DBCursor<Player> foobar = getApp().playerRepository.find(DBQuery.is("username", "foobar"));
        if (foobar.hasNext()) {
            getApp().playerRepository.deleteById(foobar.next().getId());
        }

        Form form = new Form();
        form.param("username", "foobar");
        form.param("password", "foobar");
        form.param("email", "foobar@mailinator.com");

        URI uri = UriBuilder.fromPath(BASE_URL + "/auth/register").build();
        Response response = client().target(uri)
                .request()
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(response.getLocation().getPath()).contains(uri.getPath());
    }

    @Test
    public void verifyPassword() throws Exception {
        ForgotpassDTO dto = new ForgotpassDTO();
        dto.setEmail("cash1981@mailinator.com");
        dto.setNewpassword("baz");

        URI uri = UriBuilder.fromPath(BASE_URL + "/auth/newpassword").build();
        client().target(uri).request().put(Entity.json(dto));

        Player cash = getApp().playerRepository.findById(getApp().playerId);
        assertThat(cash.getNewPassword()).isEqualTo("baz");

        Response response = client().target(BASE_URL + "/auth/verify/" + getApp().playerId)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        cash = getApp().playerRepository.findById(getApp().playerId);
        assertThat(cash.getNewPassword()).isNull();
        assertThat(cash.getPassword()).isEqualTo(DigestUtils.sha1Hex("baz"));
    }

}
