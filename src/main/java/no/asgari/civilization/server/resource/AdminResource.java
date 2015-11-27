/*
 * Copyright (c) 2015 Shervin Asgari
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

import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.model.Player;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for admin stuff
 */
@Path("admin")
@Log4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    private final GameAction gameAction;

    public AdminResource(DB db) {
        gameAction = new GameAction(db);
    }

    /**
     * Since this will go in production, for now only I am allowed to change this
     *
     * @param admin
     * @param gameid
     */
    @Path("/changeuser")
    @POST
    //public Response changeUserForGame(@Auth Player admin, @QueryParam("gameid") String gameid,
    public Response changeUserForGame(@QueryParam("gameid") String gameid,
                                      @QueryParam("fromUsername") String fromUsername,
                                      @QueryParam("toUsername") String toUsername) {

    /*    if (!admin.getUsername().equals("admin")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
*/
        gameAction.changeUserFromExistingGame(gameid, fromUsername, toUsername);

        return Response.ok().build();
    }

    /**
     * Since this will go in production, for now only I am allowed to change this
     *
     * @param admin
     * @param gameid
     */
    @Path("/deletegame")
    @POST
    public Response deleteGame(@Auth Player admin, @QueryParam("gameid") String gameid) {
        if (!"admin".equals(admin.getUsername())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        boolean deleted = gameAction.deleteGame(gameid);
        if (deleted) return Response.ok().build();

        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @Path("/email/notification/{playerId}/stop")
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response stopEmail(@PathParam("playerId") String playerId) {
        boolean yes = gameAction.disableEmailForPlayer(playerId);
        if (yes) {
            return Response.ok().entity(
                    "You will no longer get anymore emails. Don't forget to check in once in a while. " +
                            "If you reconsider and want to get emails again, then you have to manually send a mail to cash@playciv.com and ask to get emails again")
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /*
    @PUT
    @Path("/mail")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMail(@QueryParam("msg") String msg) {
        gameAction.sendMailToAll(msg);
        return Response.noContent().build();
    }
    */
}
