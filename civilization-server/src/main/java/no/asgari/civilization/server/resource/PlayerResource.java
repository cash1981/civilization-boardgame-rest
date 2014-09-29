package no.asgari.civilization.server.resource;

import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createPlayer(PlayerDTO playerDTO) {
        log.debug("Entering create player");

        PlayerAction playerAction = new PlayerAction(playerCollection);
        try {
            String playerId = playerAction.createPlayer(playerDTO);
            return Response.status(Response.Status.CREATED)
                    .location(URI.create(uriInfo.getPath() + "/" + playerId)
                    ).build();
        } catch (PlayerExistException ex) {
            return Response.status(ex.getStatus())
                    .entity(ex)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
