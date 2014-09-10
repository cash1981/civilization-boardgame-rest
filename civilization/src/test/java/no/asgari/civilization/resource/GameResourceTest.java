package no.asgari.civilization.resource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import no.asgari.civilization.representations.PBF;

import java.util.List;

public class GameResourceTest {

    static public void main(String[] args) {
        Client client = Client.create();
        WebResource resource = client.resource("http://localhost/civilization/games");
        List<PBF> pbf = resource.get(List.class);

    }
}
