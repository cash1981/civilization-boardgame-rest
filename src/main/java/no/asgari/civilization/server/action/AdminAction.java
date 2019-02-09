package no.asgari.civilization.server.action;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class AdminAction extends BaseAction {

    public void cleanup() {
        log.info("Running cleanup. Finding all aborted games, chat and gamelogs from old deleted games");

        List<String> abortedGames = pbfRepository.findAllByActiveFalseAndWinnerIsNull().stream().map(PBF::getId).collect(toList());
        log.info("Found " + abortedGames.size() + " aborted games. Deleting those.");
        abortedGames.forEach(pbfId -> pbfRepository.deleteById(pbfId));

        List<GameLog> allLogs = gameLogRepository.findAll();
        List<Chat> allChats = chatRepository.findAll();

        log.info("Before deleting, size of all game logs is " + allLogs.size());
        allLogs.parallelStream()
                .filter(gl -> abortedGames.contains(gl.getPbfId()))
                .forEach(gamelog -> gameLogRepository.deleteById(gamelog.getId()));
        log.info("After deleting, size of gamelog is " + gameLogRepository.findAll().size());

        log.info("Before deleting chat size is " + allChats.size());

        allChats.parallelStream()
                .filter(chat -> abortedGames.contains(chat.getPbfId()) || Strings.isNullOrEmpty(chat.getPbfId()))
                .forEach(c -> chatRepository.deleteById(c.getId()));

        log.info("After deleting, size of chat is " + chatRepository.findAll().size());

    }
}
