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

import com.google.common.base.Preconditions;
import com.google.common.html.HtmlEscapers;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.dto.CheckNameDTO;
import no.asgari.civilization.server.model.Player;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

@Path("auth")
@Log4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final DB db;
    private final JacksonDBCollection<Player, String> playerCollection;

    @Context
    private UriInfo uriInfo;

    public AuthResource(DB db) {
        this.db = db;
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
    }

    @POST
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@FormParam("username") @NotNull String username, @FormParam("password") @NotNull String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);

        CivAuthenticator auth = new CivAuthenticator(db);
        Optional<Player> playerOptional = auth.authenticate(new BasicCredentials(username, password));
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            URI games = uriInfo.getBaseUriBuilder()
                    .path("/player/")
                    .path(player.getId())
                    .path("/games")
                    .build();

            player.setPassword("");
            return Response.ok()
                    .entity(player)
                    .location(games)
                    .build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/register")
    public Response register(@FormParam("username") @NotNull String username, @FormParam("password") @NotNull String password, @FormParam("email") @NotNull String email) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        Preconditions.checkNotNull(email);

        PlayerAction playerAction = new PlayerAction(db);
        try {
            String playerId = playerAction.createPlayer(username, password, email);
            return Response.status(Response.Status.CREATED)
                    .location(uriInfo.getAbsolutePathBuilder().path(playerId).build())
                    .entity("{\"id\": \"" + playerId + "\"}")
                    .build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        } catch (Exception ex) {
            log.error("Unknown error when registering user: " + ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/register/check/username")
    public Response checkUsername(CheckNameDTO nameDTO) {
        Preconditions.checkNotNull(nameDTO);

        //If these doesn't match, then the username is unsafe
        if (!nameDTO.getName().equals(HtmlEscapers.htmlEscaper().escape(nameDTO.getName()))) {
            log.warn("Unsafe username " + nameDTO.getName());
            return Response.status(Response.Status.FORBIDDEN).entity("{\"invalidChars\":\"true\"}").build();
        }

        @Cleanup DBCursor<Player> dbPlayer = playerCollection.find(
                DBQuery.is("username", nameDTO.getName().trim()), new BasicDBObject());

        if (dbPlayer.hasNext()) {
            return Response.status(Response.Status.FORBIDDEN).entity("{\"isTaken\":\"true\"}").build();
        }

        return Response.ok().build();
    }

}
