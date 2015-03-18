package no.asgari.civilization.server.resource;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

public class PlayerResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/civilization", RULE.getLocalPort());

    @Before
    public void ensureCurrentPlayer() {
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(getApp().playerId))
                .findFirst().get();
        playerhand.setYourTurn(true);
        getApp().pbfCollection.updateById(getApp().pbfId, pbf);
    }


    @Test
    public void chooseTechThenRevealThenDelete() throws Exception {
        final String techToReseach = "Pottery";

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .forEach(p -> assertThat(p.getTechsChosen()).isEmpty());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", getApp().pbfId)).build();
        Response response = client().target(uri)
                .queryParam("name", techToReseach)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Pottery");

        //reveal it
        DBCursor<GameLog> gameLogs = getApp().gameLogCollection.find(DBQuery.is("pbfId", getApp().pbfId));
        if (!gameLogs.hasNext()) {
            fail("Should have gamelog");
        }

        while (gameLogs.hasNext()) {
            GameLog gameLog = gameLogs.next();
            if (gameLog.getPrivateLog() != null && gameLog.getPrivateLog().matches(".*researched Pottery.*")) {
                uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/reveal/%s", getApp().pbfId, gameLog.getId())).build();
                response = client().target(uri).request()
                        .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                        .post(null);
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            }
        }

        //Now remove it
        uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/remove", getApp().pbfId)).build();
        response = client().target(uri)
                .queryParam("name", techToReseach)
                .request()
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

    }

    @Test
    public void endTurnAndChooseNextPlayer() throws Exception {
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

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

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/endturn", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
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
        //Find another player
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        String anotherPlayerId = getAnotherplayerId(pbf);
        assertNotNull(anotherPlayerId);

        Optional<Item> item = getItemFromPlayerhand(pbf, SheetName.VILLAGES);
        if(!item.isPresent()) {
            testDrawVillage();
            item = getItemFromPlayerhand(pbf, SheetName.VILLAGES);
        }
        assertTrue(item.isPresent());

        ItemDTO itemDTO = createItemDTO(SheetName.VILLAGES, item.get().getName());
        itemDTO.setOwnerId(anotherPlayerId);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/trade", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(itemDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        //Check
        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get();
        Playerhand anotherPlayer = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(anotherPlayerId))
                .findFirst().get();

        assertThat(cash1981.getItems()).doesNotContain(item.get());
        assertThat(anotherPlayer.getItems()).contains(item.get());
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
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
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
        itemDTO.setPbfId(getApp().pbfId);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/trade", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(itemDTO), Response.class);
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void testDrawCultureCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.CULTURE_1)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testRevealItem() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);
        assertTrue(gameLogOptional.get().getDraw().getItem().isHidden());

        GameLog gameLog = getApp().gameLogCollection.findOneById(gameLogOptional.get().getId());
        if (gameLog.getDraw() != null && gameLog.getDraw().getItem() != null && gameLog.getPbfId().equals(getApp().pbfId)) {
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setSheetName(SheetName.INFANTRY.name());
            itemDTO.setName(gameLog.getDraw().getItem().getName());
            itemDTO.setPbfId(getApp().pbfId);

            URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/reveal", getApp().pbfId)).build();
            Response response = client().target(uri)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                    .put(Entity.json(itemDTO), Response.class);
            assertEquals(HttpStatus.OK_200, response.getStatus());
        } else {
            fail("Should have gamelog");
        }
    }

    @Test
    public void testDrawInfantryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.INFANTRY)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawArtilleryCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.ARTILLERY)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawGreatPersonCard() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.GREAT_PERSON)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testDrawVillage() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, SheetName.VILLAGES)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testTech() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/%s", getApp().pbfId, SheetName.LEVEL_1_TECH)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(response.getStatus(), HttpStatus.METHOD_NOT_ALLOWED_405);
    }

    @Test
    public void testDrawingInvalid() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/draw/%s/%s", getApp().pbfId, "foobar")).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(null);
        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void discardItem() throws Exception {
        testDrawVillage();

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(pbf.getDiscardedItems()).isEmpty();
        Item village = getItemFromPlayerhand(pbf, SheetName.VILLAGES).get();
        assertNotNull(village);

        ItemDTO itemDTO = createItemDTO(SheetName.VILLAGES, village.getName());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/discard", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.json(itemDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(pbf.getDiscardedItems()).isNotEmpty();
    }

    @Test
    public void discardUnit() throws Exception {
        testDrawInfantryCard();
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Item infantry = getItemFromPlayerhand(pbf, SheetName.INFANTRY).get();
        assertNotNull(infantry);

        ItemDTO itemDTO = createItemDTO(SheetName.INFANTRY, infantry.getName());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/item/discard", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .post(Entity.json(itemDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    private ItemDTO createItemDTO(SheetName sheetName, String itemName) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setSheetName(sheetName.name());
        itemDTO.setName(itemName);
        itemDTO.setOwnerId(getApp().playerId);
        itemDTO.setPbfId(getApp().pbfId);
        return itemDTO;
    }

    private Optional<Item> getItemFromPlayerhand(PBF pbf, SheetName sheetName) {
        return pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get()
                .getItems().stream()
                .filter(fil -> fil.getSheetName() == sheetName)
                .findFirst();
    }

}
