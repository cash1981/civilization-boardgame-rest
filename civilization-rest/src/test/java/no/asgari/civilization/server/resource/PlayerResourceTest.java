package no.asgari.civilization.server.resource;

import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void chooseTechThenRevealThenDelete() throws Exception {
        final String techToReseach = "Pottery";
        
        PBF pbf = pbfCollection.findOneById(pbfId);
        pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .forEach(p -> assertThat(p.getTechsChosen()).isEmpty());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .queryParam("name",techToReseach)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        pbf = pbfCollection.findOneById(pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Pottery");

        //reveal it
        DBCursor<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId));
        if (!gameLogs.hasNext()) {
            Assert.fail("Should have gamelog");
        }

        while (gameLogs.hasNext()) {
            GameLog gameLog = gameLogs.next();
            if (gameLog.getPrivateLog() != null && gameLog.getPrivateLog().matches(".*researched Pottery.*")) {
                uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/reveal/%s", RULE.getLocalPort(), pbfId, gameLog.getId())).build();
                response = client().resource(uri)
                        .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                        .put(ClientResponse.class);
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            }
        }

        //Now remove it
        uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/remove", RULE.getLocalPort(), pbfId)).build();
        response = client().resource(uri)
                .queryParam("name",techToReseach)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .delete(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

    }

    @Test
    public void endTurnAndChooseNextPlayer() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);

        int i = -1;
        Playerhand nextPlayer = null;
        boolean found = false;
        for (Playerhand p : pbf.getPlayers()) {
            i++;
            if (p.getUsername().equals("cash1981")) {
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
        for (Playerhand p : pbf.getPlayers()) {
            if (p.equals(nextPlayer)) {
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
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void testDrawCultureCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, SheetName.CULTURE_1)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testRevealItem() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);
        assertTrue(gameLogOptional.get().getDraw().getItem().isHidden());

        GameLog gameLog = gameLogCollection.findOneById(gameLogOptional.get().getId());
        if (gameLog.getDraw() != null && gameLog.getDraw().getItem() != null && gameLog.getPbfId().equals(pbfId)) {
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setSheetName(SheetName.INFANTRY.name());
            itemDTO.setName(gameLog.getDraw().getItem().getName());
            itemDTO.setPbfId(pbfId);

            URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/reveal", RULE.getLocalPort(), pbfId)).build();
            ClientResponse response = client().resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                    .entity(itemDTO)
                    .put(ClientResponse.class);
            assertEquals(HttpStatus.OK_200, response.getStatus());
        } else {
            fail("Should have gamelog");
        }
    }

    @Test
    public void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, SheetName.INFANTRY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, SheetName.ARTILLERY)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawGreatPersonCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, SheetName.GREAT_PERSON)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawVillage() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, SheetName.VILLAGES)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testTech() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/%s", RULE.getLocalPort(), pbfId, SheetName.LEVEL_1_TECH)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.METHOD_NOT_ALLOWED_405);
    }

    @Test
    public void testDrawingInvalid() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", RULE.getLocalPort(), pbfId, "foobar")).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(ClientResponse.class);
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

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/discard", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());

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

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/discard", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(itemDTO)
                .post(ClientResponse.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
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
