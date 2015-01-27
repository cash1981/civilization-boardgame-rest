package no.asgari.civilization.server.resource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import junit.framework.Assert;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.*;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

public class PlayerResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivilizationConfiguration> RULE =
            new DropwizardAppRule<CivilizationConfiguration>(CivilizationApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";

    @Before
    public void ensureCurrentPlayer() {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst().get();
        playerhand.setYourTurn(true);
        pbfCollection.updateById(pbfId, pbf);
    }

    @Test
    public void chooseSpecificTechForLoggedInPlayer() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);
        pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .forEach(p -> assertThat(p.getTechsChosen()).isEmpty());

        ItemDTO dto = new ItemDTO();
        dto.setName("Agriculture");

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(dtoAsJSon)
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        pbf = pbfCollection.findOneById(pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Agriculture");
    }

    @Test
    public void endTurnAndChooseNextPlayer() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);

        int i = -1;
        Playerhand nextPlayer = null;
        boolean found = false;
        for(Playerhand p : pbf.getPlayers()) {
            i++;
            if(p.getUsername().equals("cash1981")) {
                nextPlayer = pbf.getPlayers().get(++i);
                assertFalse(nextPlayer.isYourTurn());
                found = true;
                break;
            }
        }
        assertTrue(found);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/endturn", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

        pbf = pbfCollection.findOneById(pbfId);
        found = false;
        for(Playerhand p : pbf.getPlayers()) {
            if(p.equals(nextPlayer)) {
                assertTrue(p.isYourTurn());
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testThatYouCanTradeItems() throws Exception {
        testDrawVillage();

        //Find another player
        PBF pbf = pbfCollection.findOneById(pbfId);
        String anotherPlayerId = getAnotherplayerId(pbf);
        assertNotNull(anotherPlayerId);

        Item village = getItemFromPlayerhand(pbf, SheetName.VILLAGES);
        assertNotNull(village);

        ItemDTO itemDTO = createItemDTO(SheetName.VILLAGES, village.getName());
        itemDTO.setOwnerId(anotherPlayerId);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/trade", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .put(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        //Check
        pbf = pbfCollection.findOneById(pbfId);
        Playerhand cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get();
        Playerhand anotherPlayer = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(anotherPlayerId))
                .findFirst().get();

        assertThat(cash1981.getItems()).doesNotContain(village);
        assertThat(anotherPlayer.getItems()).contains(village);
    }

    private String getAnotherplayerId(PBF pbf) {
        return pbf.getPlayers().stream()
                .filter(p -> !p.getUsername().equals("cash1981"))
                .findFirst().get().getPlayerId();
    }

    @Test
    public void checkThatYouGetExceptionWhenTradingStuffWhichIsNotAllowed() throws Exception {
        testDrawArtilleryCard();

        //Find another player
        PBF pbf = pbfCollection.findOneById(pbfId);
        String anotherPlayerId = getAnotherplayerId(pbf);
        assertNotNull(anotherPlayerId);

        Item artillery = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get()
                .getItems().stream()
                .filter(fil -> fil.getSheetName() == SheetName.ARTILLERY)
                .findFirst().get();

        assertNotNull(artillery);

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setSheetName(SheetName.ARTILLERY.name());
        itemDTO.setName(artillery.getName());
        itemDTO.setOwnerId(anotherPlayerId);
        itemDTO.setPbfId(pbfId);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/trade", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
        assertEquals(response.getEntity(String.class), "Could not find item");
    }

    @Test
    public void testDrawCultureCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.CULTURE_1)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void testRevealItem() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);

        GameLog gameLog = gameLogCollection.findOneById(gameLogOptional.get().getId());
        if(gameLog.getDraw() != null && gameLog.getDraw().getItem() != null && gameLog.getPbfId().equals(pbfId)) {
            URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/revealItem/%s", RULE.getLocalPort(), pbfId, gameLog.getId())).build();
            ClientResponse response = client().resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                    .put(ClientResponse.class);
            assertEquals(response.getStatus(), HttpStatus.OK_200);
        } else {
            fail("Should have gamelog");
        }
    }

    @Test
    public void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.INFANTRY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.ARTILLERY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void testDrawGreatPersonCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.GREAT_PERSON)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void testDrawVillage() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.VILLAGES)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void testTech() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, SheetName.LEVEL_1_TECH)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.NOT_MODIFIED_304);
    }

    @Test
    public void testDrawingInvalid() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/draw/%s", RULE.getLocalPort(), pbfId, "foobar")).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void drawUnitsForBattle() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        DBCursor<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId));
        int count = gameLogs.count();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/draw", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .queryParam("numOfUnits", "3")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
        List list = response.getEntity(List.class);
        assertThat(list).hasSize(3);

        gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId));
        assertThat(gameLogs.count()).isEqualTo(count+3);

        //finally end battle
        endBattle();
    }

    private void endBattle() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/end", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void drawUnitsWithNoParam() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/draw", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
        List list = response.getEntity(List.class);
        assertThat(list).hasSize(0);
    }

    @Test
    public void drawUnitsWithInvalidParamShouldResult404() throws Exception {
        testDrawArtilleryCard();
        testDrawInfantryCard();
        testDrawArtilleryCard();

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/battle/draw", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client()
                .resource(uri)
                .queryParam("numOfUnits", "foobar")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void discardItem() throws Exception {
        testDrawVillage();

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf.getDiscardedItems()).isEmpty();
        Item village = getItemFromPlayerhand(pbf, SheetName.VILLAGES);
        assertNotNull(village);

        ItemDTO itemDTO = createItemDTO(SheetName.VILLAGES, village.getName());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .delete(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);

        pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf.getDiscardedItems()).isNotEmpty();
    }

    @Test
    public void discardUnit() throws Exception {
        testDrawInfantryCard();
        PBF pbf = pbfCollection.findOneById(pbfId);
        Item infantry = getItemFromPlayerhand(pbf, SheetName.INFANTRY);
        assertNotNull(infantry);

        ItemDTO itemDTO = createItemDTO(SheetName.INFANTRY, infantry.getName());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .delete(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    private ItemDTO createItemDTO(SheetName sheetName, String itemName) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setSheetName(sheetName.name());
        itemDTO.setName(itemName);
        itemDTO.setOwnerId(playerId);
        itemDTO.setPbfId(pbfId);
        return itemDTO;
    }

    private Item getItemFromPlayerhand(PBF pbf, SheetName sheetName) {
        return pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get()
                .getItems().stream()
                .filter(fil -> fil.getSheetName() == sheetName)
                .findFirst().get();
    }


}
