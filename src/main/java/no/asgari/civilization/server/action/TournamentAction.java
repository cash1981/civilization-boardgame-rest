//package no.asgari.civilization.server.action;
//
//import com.mongodb.DB;
//import no.asgari.civilization.server.email.SendEmail;
//import no.asgari.civilization.server.model.Player;
//import no.asgari.civilization.server.model.tournament.Tournament;
//import no.asgari.civilization.server.model.tournament.TournamentPlayer;
//import org.mongojack.JacksonDBCollection;
//
//import java.util.List;
//
//public class TournamentAction extends BaseAction {
//    private final JacksonDBCollection<Player, String> playerCollection;
//    private final JacksonDBCollection<Tournament, String> tournamentCol;
//
//    public TournamentAction(DB db) {
//        super(db);
//        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
//        this.tournamentCol = JacksonDBCollection.wrap(db.getCollection(Tournament.COL_NAME), Tournament.class, String.class);
//
//    }
//
//    public boolean signup(Player player, int tournamentNumber) {
//        List<Tournament> tournaments = tournamentCol.find().toArray();
//        if (tournaments == null || tournaments.isEmpty()) {
//            Tournament tournament = createTournament(tournamentNumber);
//            tournament.getPlayers().add(new TournamentPlayer(player));
//            return SendEmail.someoneJoinedTournament(player);
//        }
//
//        Tournament tournament = tournaments.get(tournamentNumber - 1);
//        if (tournament.getPlayers().contains(new TournamentPlayer(player))) {
//            return false;
//        }
//
//        tournament.getPlayers().add(new TournamentPlayer(player));
//        tournamentCol.save(tournament);
//
//        return SendEmail.someoneJoinedTournament(player);
//    }
//
//    private Tournament createTournament(int nr) {
//        Tournament t = new Tournament();
//        t.setName("First tournament");
//        t.setTournamentNumber(nr);
//        tournamentCol.save(t);
//        return t;
//    }
//
//    public List<Tournament> getTournaments() {
//        return tournamentCol.find().toArray();
//    }
//}
