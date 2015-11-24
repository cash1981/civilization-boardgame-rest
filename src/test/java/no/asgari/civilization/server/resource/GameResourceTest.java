package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.dto.ChatDTO;
import no.asgari.civilization.server.dto.CheckNameDTO;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/api", RULE.getLocalPort());

    @Test
    public void shouldGetActiveGames() {
        //Response response =
        Client client = ClientBuilder.newClient();
        List list = client.target(BASE_URL + "/game")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(List.class);
        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(list).isNotEmpty();
    }

    @Test
    public void getAllActiveGamesForPlayer() throws Exception {
        URI uri = UriBuilder.fromPath(BASE_URL + "/game/player").build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(Response.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void getWinners() throws Exception {
        URI uri = UriBuilder.fromPath(BASE_URL + "/game/winners").build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void createGame() throws Exception {
        CreateNewGameDTO dto = new CreateNewGameDTO();
        dto.setNumOfPlayers(4);
        dto.setName("PBF WaW");
        dto.setType(GameType.WAW);
        dto.setColor(Playerhand.blue());

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(BASE_URL + "/game").build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.json(dtoAsJSon));


        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        URI location = response.getLocation();
        assertThat(location.getPath()).isNotEmpty();
        String id = location.getPath().split(BASE_URL)[0];
        assertThat(id.charAt(0)).isEqualTo('/');

        assertThat(getApp().pbfCollection.findOneById(id.substring(1)).getPlayers().size()).isEqualTo(1);
    }

    @Test
    public void shouldGetAllAvailableTechs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/techs", getApp().pbfId))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(ArrayList.class);

        assertThat(list).isNotEmpty();
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(list.size()).isEqualTo(pbf.getTechs().size() - 1);
    }

    @Test
    public void getAllPublicGameLogs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/publiclog", getApp().pbfId))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .get(ArrayList.class);
        assertThat(list).isNotEmpty();
    }

    @Test
    public void getAllPrivateGameLogs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/privatelog", getApp().pbfId))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(ArrayList.class);
        assertThat(list).isNotEmpty();
    }

    private void chooseTechForPlayer() throws JsonProcessingException {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", getApp().pbfId)).build();
        Response response = client().target(uri)
                .queryParam("name", "Agriculture")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Agriculture");
    }

    @Test
    public void checkThatEncodingInFrontendGivesSameAsBackend() {
        String encodingFrontend = "Basic Y2FzaDE5ODE6Zm9v";
        assertEquals(encodingFrontend, getUsernameAndPassEncoded());
    }

    @Test
    public void getGameAsPublicUser() throws Exception {
        PBF pbf = getApp().pbfCollection.findOne();

        Response response = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s", pbf.getId()))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getEntity()).isNotNull();
    }

    @Test
    public void joinGameAndThenWithdraw() throws Exception {
        Response response = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/join", getApp().pbfId_3))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAdminEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(204);

        Response secondResponse = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/withdraw", getApp().pbfId_3))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAdminEncoded())
                .post(null);

        assertThat(secondResponse.getStatus()).isEqualTo(204);
    }

    @Test
    public void getListOfPlayersInGame() throws Exception {
        PBF pbf = getApp().pbfCollection.findOne();

        Response response = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/players", pbf.getId()))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        List players = response.readEntity(List.class);
        assertThat(players).isNotNull();
        assertThat(players).hasSize(4);
    }

    @Test
    public void getListOfPlayersInGameExceptCurrentLoggedIn() throws Exception {
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        int players = pbf.getPlayers().size() - 1;

        Response response = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/players", pbf.getId()))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        List playersList = response.readEntity(List.class);
        assertThat(playersList).isNotNull();
        assertThat(playersList.size()).isEqualTo(players);
    }

    @Test
    public void chatTest() {
        PBF pbf = getApp().pbfCollection.findOne();
        Form form = new Form("message", "Chat message");
        Response response = client().target(
                UriBuilder.fromPath(BASE_URL + String.format("/game/%s/chat", pbf.getId())).build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.form(form), Response.class);

        List<ChatDTO> chat = response.readEntity(List.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(chat).hasSize(1);

        response = client().target(
                UriBuilder.fromPath(BASE_URL + String.format("/game/%s/chat", pbf.getId())).build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get();

        List chats = response.readEntity(List.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(chats).hasSize(1);
    }


    @Test
    public void publicChatTest() throws Exception {
        Form form = new Form("message", "public chat message");
        Response response = client().target(
                UriBuilder.fromPath(BASE_URL + "/game/publicchat").build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.form(form), Response.class);

        List<ChatDTO> chat = response.readEntity(List.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(chat).hasSize(1);

        response = client().target(
                UriBuilder.fromPath(BASE_URL + "/game/publicchat").build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get();

        List chats = response.readEntity(List.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(chats).hasSize(1);
    }

    @Test
    public void endGame() throws Exception {
        //First create
        CreateNewGameDTO dto = new CreateNewGameDTO();
        dto.setNumOfPlayers(2);
        dto.setName("PBF WaW2");
        dto.setType(GameType.WAW);
        dto.setColor(Playerhand.blue());

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(BASE_URL + "/game").build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.json(dtoAsJSon));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);

        URI location = response.getLocation();
        assertThat(location.getPath()).isNotEmpty();
        String id = location.getPath().split(BASE_URL)[0];
        assertThat(id.charAt(0)).isEqualTo('/');

        Response secondResponse = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/end", id.substring(1)))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .delete();

        assertThat(secondResponse.getStatus()).isEqualTo(204);
    }

    @Test
    public void withdrawShouldPutInWithdrawInPBF() throws Exception {
        //First ensure its your turn
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("Itchi"))
                .findFirst().get();
        playerhand.setYourTurn(true);
        getApp().pbfCollection.updateById(getApp().pbfId, pbf);

        //Må draw noe først, også teste den etterpå
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.ARTILLERY)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getItchiEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(pbf.getWithdrawnPlayers()).isEmpty();

        Response post = client().target(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/withdraw", getApp().pbfId))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getItchiEncoded())
                .post(null);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(pbf.getWithdrawnPlayers()).isNotEmpty();

        assertThat(post.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void addMapLink() throws Exception {
        PBF pbf = getApp().pbfCollection.findOne();
        assertThat(pbf.getMapLink()).isNullOrEmpty();
        Form form = new Form("link", "https://docs.google.com/presentation/d/1hgP0f6hj4-lU6ysdOb02gd7oC5gXo8zAAke4RhgIt54/edit?usp=sharing");
        Response response = client().target(
                UriBuilder.fromPath(BASE_URL + String.format("/game/%s/map", pbf.getId())).build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.form(form), Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        MessageDTO message = response.readEntity(MessageDTO.class);
        assertThat(message.getMessage()).isEqualTo("1hgP0f6hj4-lU6ysdOb02gd7oC5gXo8zAAke4RhgIt54");

        pbf = getApp().pbfCollection.findOneById(pbf.getId());
        assertThat(pbf.getMapLink()).isEqualToIgnoringCase("1hgP0f6hj4-lU6ysdOb02gd7oC5gXo8zAAke4RhgIt54");
    }

    @Test
    public void addAssetLink() throws Exception {
        PBF pbf = getApp().pbfCollection.findOne();
        assertThat(pbf.getAssetLink()).isNullOrEmpty();
        Form form = new Form("link", "https://docs.google.com/spreadsheets/d/10-syTLb2i2NdB8T_alH9KeyzT8FTlBK6Csmc_Hjjir8/pubhtml?widget=true&amp;headers=false");
        Response response = client().target(
                UriBuilder.fromPath(BASE_URL + String.format("/game/%s/asset", pbf.getId())).build())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.form(form), Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        MessageDTO message = response.readEntity(MessageDTO.class);
        assertThat(message.getMessage()).isEqualTo("10-syTLb2i2NdB8T_alH9KeyzT8FTlBK6Csmc_Hjjir8");
        pbf = getApp().pbfCollection.findOneById(pbf.getId());
        assertThat(pbf.getAssetLink()).isEqualToIgnoringCase("10-syTLb2i2NdB8T_alH9KeyzT8FTlBK6Csmc_Hjjir8");
    }

    @Test
    public void getSpecificGame() throws Exception {
        //Response response =
        Client client = ClientBuilder.newClient();
        Response response = client.target(BASE_URL + "/game/" + getApp().pbfId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void getPublicTurns() throws Exception {
        //Response response =
        Client client = ClientBuilder.newClient();
        Response response = client.target(BASE_URL + "/game/" + getApp().pbfId + "/turns")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

}