/*
 * Copyright (c) 2015-2021 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Unit;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Path("draw/{pbfId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log4j
public class DrawResource {

    private final DB db;
    private final DrawAction drawAction;

    @Context
    private UriInfo uriInfo;

    public DrawResource(DB db) {
        this.db = db;
        this.drawAction = new DrawAction(db);
    }

    /**
     * Rest endpoint that draws a random item from playerhand and gives to another player
     *
     * @param pbfId          - The pbf id
     * @param targetPlayerId - The targeted player which will recieve the item
     * @param sheetName      - the item to be automatically drawn from playerhand and given to another player
     * @param player         - The logged in player that we will take the item from
     */
    @POST
    @Timed
    @Path("/{sheetName}/loot/{targetPlayerId}")
    public Response loot(@PathParam("pbfId") String pbfId, @PathParam("sheetName") String sheetName, @PathParam("targetPlayerId") String targetPlayerId, @Auth Player player) {
        //Check that it is tradable
        Optional<SheetName> sheetNameOptional = SheetName.find(sheetName);
        if (!sheetNameOptional.isPresent() && !sheetName.equals("Culture Card")) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MessageDTO("Could not find item " + sheetName))
                    .build();
        } else if (!sheetNameOptional.isPresent() && sheetName.equals("Culture Card")) {
            Item item = drawAction.loot(pbfId, SheetName.CULTURE_CARD, targetPlayerId, player.getId());
            return Response.ok().entity(item).build();

        } else {
            Item item = drawAction.loot(pbfId, EnumSet.of(sheetNameOptional.get()), targetPlayerId, player.getId());
            return Response.ok().entity(item).build();
        }
    }

    @PUT
    @Timed
    @Path("/battlehand/reveal")
    public Response revealAndDiscardBattlehand(@PathParam("pbfId") String pbfId, @Auth Player player) {
        drawAction.revealAndDiscardBattlehand(pbfId, player.getId());
        return Response.ok().build();
    }

    @POST
    @Path("/{sheetName}")
    @Timed
    public Response drawItem(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("sheetName") String sheetNameString) {
        Optional<SheetName> sheetNameOptional = SheetName.find(sheetNameString);
        if (!sheetNameOptional.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, player.getId(), sheetNameOptional.get());
        if (gameLogOptional.isPresent())
            return Response.ok().build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Draws units from playerhand for battle purposes
     *
     * @param player
     * @param pbfId
     * @param numberOfunits
     * @return
     */
    @PUT
    @Path("/battle")
    @Timed
    public Response drawUnits(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @QueryParam("numOfUnits") int numberOfunits) {
        List<Unit> units = drawAction.drawUnitsFromBattlehandForBattle(pbfId, player.getId(), numberOfunits);
        return Response.ok().entity(units).build();
    }

    /**
     * Draws barbarians
     *
     * @param player
     * @param pbfId
     * @return 200 ok
     */
    @PUT
    @Path("/battle/barbarians")
    @Timed
    public Response drawBarbarians(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        List<Unit> units = drawAction.drawBarbarians(pbfId, player.getId());
        return Response.ok().entity(units).build();
    }

    /**
     * Discard barbarians
     *
     * @param player
     * @param pbfId
     * @return 201 no content
     */
    @POST
    @Path("/battle/discard/barbarians")
    @Timed
    public Response discardBarbarians(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        drawAction.discardBarbarians(pbfId, player.getId());
        return Response.noContent().build();
    }

}
