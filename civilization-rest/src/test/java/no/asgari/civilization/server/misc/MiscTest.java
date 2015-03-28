package no.asgari.civilization.server.misc;

import no.asgari.civilization.server.dto.MessageDTO;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;

import static org.assertj.core.api.Assertions.assertThat;

public class MiscTest {

    @Test
    public void checkDecoding() throws Exception {
        String encodedText = "http%3A%2F%2Fw3schools.com%2Fmy%20test.asp%3Fname%3Dst%C3%A5le%26car%3Dsaab";
        String decodedText = URLDecoder.decode(encodedText, "UTF-8");
        assertThat(decodedText).isEqualTo("http://w3schools.com/my test.asp?name=ståle&car=saab");

        encodedText = "Lets%20get%20going%20foobars";
        decodedText = URLDecoder.decode(encodedText, "UTF-8");
        assertThat(decodedText).isEqualTo("Lets get going foobars");
    }

    @Test
    public void createEntityFromString() throws Exception {
        Response badReq = Response.status(Response.Status.BAD_REQUEST)
                .entity(new MessageDTO("foobar"))
                .build();

        assertThat(((MessageDTO)badReq.getEntity()).getMessage()).isEqualTo("foobar");
    }

}
