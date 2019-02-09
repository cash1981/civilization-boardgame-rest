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

import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.action.AdminAction;
import no.asgari.civilization.server.action.GameAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Resource for admin stuff
 */
@RestController
@RequestMapping("admin") //TODO Må lage spring security for denne
@Slf4j
public class AdminResource {

    private final GameAction gameAction;
    private final AdminAction adminAction;

    @Autowired
    public AdminResource(GameAction gameAction, AdminAction adminAction) {
        this.gameAction = gameAction;
        this.adminAction = adminAction;
    }

    /**
     * Since this will go in production, for now only I am allowed to change this
     *
     * @param gameid
     */
    @PostMapping("/changeuser")
    public ResponseEntity changeUserForGame(@RequestParam("gameid") String gameid,
                                            @RequestParam("fromUsername") String fromUsername,
                                            @RequestParam("toUsername") String toUsername) {

        gameAction.changeUserFromExistingGame(gameid, fromUsername, toUsername);

        return ResponseEntity.ok().build();
    }

    /**
     * Since this will go in production, for now only I am allowed to change this
     *
     * @param gameid
     */
    @PostMapping("/deletegame")
    public ResponseEntity deleteGame(@RequestParam("gameid") String gameid) {
        boolean deleted = gameAction.deleteGame(gameid);
        if (deleted) return ResponseEntity.ok().build();

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @GetMapping(value = "/email/notification/{playerId}/stop", produces = MediaType.TEXT_HTML_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity stopEmail(@PathVariable("playerId") String playerId, HttpServletRequest request) {
        boolean yes = gameAction.disableEmailForPlayer(playerId);
        if (yes) {
            String startEmailUrl = request.getRequestURI().replaceAll("stop", "start");

            return ResponseEntity.ok().body(
                    "<html><body>" +
                            "<h1>You will no longer get anymore emails. Don't forget to check in once in a while</h1> " +
                            "If you reconsider and want to get emails again, then push <a href=\""
                            + startEmailUrl + "\">here</a>" +
                            "</body></html>");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping(value = "/email/notification/{playerId}/start", produces = MediaType.TEXT_HTML_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity startEmail(@PathVariable("playerId") String playerId) {
        boolean yes = gameAction.startEmailForPlayer(playerId);
        if (yes) {
            return ResponseEntity.ok().body(
                    "<h1>Your email has started again</h1>");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/cleanup")
    public void deleteUnusedLogs() {
        adminAction.cleanup();
    }


    @PutMapping(value = "/mail", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity sendMail(@RequestParam("msg") String msg) {
        //gameAction.sendMailToAll(msg);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/taketurn", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public void takeTurn(@RequestParam("gameid") String gameid,
                         @RequestParam("username") String fromUsername) {

        gameAction.takeTurn(gameid, fromUsername);

    }
}
