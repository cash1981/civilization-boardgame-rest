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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    private final DB db;
    private final GameAction gameAction;

    public AdminResource(DB db) {
        this.db = db;
        gameAction = new GameAction(db);
    }


    /**
     * Since this will go in production, for now only I am allowed to change this
     * @param admin
     * @param gameid
     */
    @Path("/changeuser")
    @POST
    public Response changeUserForGame(@Auth Player admin, @QueryParam("gameid") String gameid,
                                      @QueryParam("fromUsername") String fromUsername,
                                      @QueryParam("toUsername") String toUsername) {
        if(!admin.getUsername().equals("cash1981")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        gameAction.changeUserFromExistingGame(gameid, fromUsername, toUsername);

        return Response.ok().build();
    }
}
