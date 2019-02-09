package no.asgari.civilization.server.resource;

import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.action.TournamentAction;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.tournament.Tournament;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("tournament")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {
    private final TournamentAction tournamentAction;

    public TournamentResource(DB db) {
        this.tournamentAction = new TournamentAction(db);
    }

    @Path("/signup/{tournamentNumber}")
    @PUT
    public Response signup(@Auth Player player, @PathParam("tournamentNumber") int tournamentNumber) {
        boolean signedup = tournamentAction.signup(player, tournamentNumber);
        if (signedup)
            return Response.ok().entity(getTournaments()).build();
        else return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    public List<Tournament> getTournaments() {
        return tournamentAction.getTournaments();
    }
}
