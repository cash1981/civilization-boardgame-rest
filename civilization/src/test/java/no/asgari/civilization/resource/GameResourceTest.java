package no.asgari.civilization.resource;

import com.mongodb.DBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import no.asgari.civilization.representations.PBF;

import java.util.List;

public class GameResourceTest {

    static public void main(String[] args) {
        Client client = Client.create();
        WebResource resource = client.resource("http://localhost:8080/games");
        List<DBObject> pbf = resource.get(List.class);

    }
}
