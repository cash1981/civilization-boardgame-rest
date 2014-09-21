package no.asgari.civilization.server.resource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoginResourceTest {

    @Test
    public void shouldGet403WithWrongUsernamePass() {

        Client client = Client.create();

        WebResource resource = client.resource(
                UriBuilder.fromPath("http://localhost:8080/login")
                        .queryParam("username", "cash1981")
                        .queryParam("password", "fifafoo")
                        .build()
        );


        ClientResponse response = resource.post(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);

    }
}
