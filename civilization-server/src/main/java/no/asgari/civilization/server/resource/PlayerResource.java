package no.asgari.civilization.server.resource;

import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/player")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class PlayerResource {
    @Context
    private UriInfo uriInfo;

    private final JacksonDBCollection<Player, String> playerCollection;

    public PlayerResource(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    //TODO Add @Valid on PlayerDTO, right now it doesnt work
    public Response createPlayer(PlayerDTO playerDTO) {
        log.debug("Entering create player");

        PlayerAction playerAction = new PlayerAction(playerCollection);
        try {
            String playerId = playerAction.createPlayer(playerDTO);
            return Response.status(Response.Status.CREATED)
                    .location(URI.create(uriInfo.getPath() + "/" + playerId)
                    ).build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deletePlayer(@PathParam("id") String playerId) {
        //Throws IllegalArgumentException if id not found, so NOT_FOUND is never returned, but 500 servlet error instead
        WriteResult<Player, String> result = playerCollection.removeById(playerId);
        if(result.getError() == null)
            return Response.status(Response.Status.NO_CONTENT).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
