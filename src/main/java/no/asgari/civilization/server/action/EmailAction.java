package no.asgari.civilization.server.action;

import io.micrometer.core.instrument.util.StringUtils;
import no.asgari.civilization.server.email.SendEmail;
import no.asgari.civilization.server.exception.NotFoundException;
import no.asgari.civilization.server.misc.CivUtil;
import no.asgari.civilization.server.model.PBF;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailAction extends BaseAction {

    @Async
    public void sendEmailNewGameCreated(String gameName) {
        playerRepository.findAll().stream()
                .filter(p -> !p.isDisableEmail())
                .filter(CivUtil::shouldSendEmail)
                .forEach(p -> SendEmail.sendMessage(p.getEmail(), "New Civilization game created",
                        "A new game by the name " + gameName + " was just created! Visit " + SendEmail.URL + " to join the game.", p.getId()));
    }

    @Async
    public void sendChat(String pbfId, String message, String username, String chat) {
        if (pbfId != null) {
            PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);

            if (StringUtils.isNotBlank(message)) {
                pbf.getPlayers()
                        .stream()
                        .filter(p -> !p.getUsername().equals(username))
                        .filter(CivUtil::shouldSendEmailInGame)
                        .forEach(p -> {
                                    SendEmail.sendMessage(p.getEmail(), "New Chat", username + " wrote in the chat: " + chat
                                            + ".\nLogin to " + SendEmail.gamelink(pbfId) + " to see the chat", p.getPlayerId());
                                }
                        );
            }
        }
    }

    @Async
    public void gameEnd(PBF pbf) {
        pbf.getPlayers().forEach(p -> SendEmail.sendMessage(p.getEmail(), "Game ended", pbf.getName() + " has ended. I hope you enjoyed playing.\n" +
                "If you like this game, please consider donating. You can find the link at the bottom of the site. It will help keep the lights on, and continue adding more features!" +
                "\n\nBest regards Shervin Asgari aka Cash", p.getPlayerId()));
    }
}
