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

package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.mongodb.DB;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.ChatDTO;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.DrawDTO;
import no.asgari.civilization.server.dto.GameDTO;
import no.asgari.civilization.server.dto.GameLogDTO;
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.dto.WinnerDTO;
import no.asgari.civilization.server.email.SendEmail;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.misc.CivUtil;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Log4j
public class GameAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Player, String> playerCollection;
    private final GameLogAction gameLogAction;
    private final JacksonDBCollection<Chat, String> chatCollection;

    public GameAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.chatCollection = JacksonDBCollection.wrap(db.getCollection(Chat.COL_NAME), Chat.class, String.class);
        this.gameLogAction = new GameLogAction(db);
    }

    public String createNewGame(CreateNewGameDTO dto, String playerId) {
        PBF pbf = new PBF();
        pbf.setName(dto.getName());
        pbf.setType(dto.getType());
        pbf.setNumOfPlayers(dto.getNumOfPlayers());
        ItemReader itemReader = new ItemReader();
        try {
            itemReader.readItemsFromExcel(dto.getType());
        } catch (IOException e) {
            log.error("Couldn't read Excel document " + e.getMessage(), e);
        }

        pbf.getItems().addAll(itemReader.shuffledCivs);
        pbf.getItems().addAll(itemReader.shuffledCultureI);
        pbf.getItems().addAll(itemReader.shuffledCultureII);
        pbf.getItems().addAll(itemReader.shuffledCultureIII);
        pbf.getItems().addAll(itemReader.shuffledGPs);
        pbf.getItems().addAll(itemReader.shuffledHuts);
        pbf.getItems().addAll(itemReader.shuffledVillages);
        pbf.getItems().addAll(itemReader.shuffledTiles);
        pbf.getItems().addAll(itemReader.shuffledCityStates);
        pbf.getItems().addAll(itemReader.ancientWonders);
        pbf.getItems().addAll(itemReader.medievalWonders);
        pbf.getItems().addAll(itemReader.modernWonders);
        pbf.getItems().addAll(itemReader.mountedList);
        pbf.getItems().addAll(itemReader.aircraftList);
        pbf.getItems().addAll(itemReader.artilleryList);
        pbf.getItems().addAll(itemReader.infantryList);
        pbf.getTechs().addAll(itemReader.allTechs);
        pbf.getSocialPolicies().addAll(itemReader.socialPolicies);

        WriteResult<PBF, String> pbfInsert = pbfCollection.insert(pbf);
        pbf.setId(pbfInsert.getSavedId());
        log.info("PBF game created with id " + pbfInsert.getSavedId());
        joinGame(pbf, playerId, Optional.of(dto.getColor()), true);

        playerCollection.find().toArray().stream()
                .filter(p -> !p.isDisableEmail())
                .forEach(p -> {
                    if (CivUtil.shouldSendEmail(p)) {
                        SendEmail.sendMessage(p.getEmail(), "New Civilization game created",
                                "A new game by the name " + pbf.getName() + " was just created! Visit " + SendEmail.URL + " to join the game.", p.getId());
                        playerCollection.updateById(p.getId(), p);
                    }
                });
        return pbf.getId();
    }

    /**
     * Returns all games sorted on active first
     *
     * @return
     */
    public List<PbfDTO> getAllGames() {
        List<PBF> pbfs = pbfCollection.find().toArray();
        return pbfs.stream()
                .map(GameAction::createPbfDTO)
                .sorted((o1, o2) -> {
                    int v = Boolean.valueOf(o1.isActive()).compareTo(o2.isActive());
                    if (v != 0) return v;
                    return Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                })
                .collect(toList());
    }

    /**
     * Creating PbfDTO so to not include every players Ghand and information
     *
     * @param pbf - the PBF
     * @return PbfDto
     */
    private static PbfDTO createPbfDTO(PBF pbf) {
        PbfDTO dto = new PbfDTO();
        dto.setType(pbf.getType());
        dto.setId(pbf.getId());
        dto.setName(pbf.getName());
        dto.setActive(pbf.isActive());
        long created = pbf.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        dto.setCreated(created);
        dto.setNumOfPlayers(pbf.getNumOfPlayers());
        dto.setPlayers(pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p, pbf.getId()))
                .collect(toList()));
        dto.setNameOfUsersTurn(pbf.getNameOfUsersTurn());
        return dto;
    }

    private static PlayerDTO createPlayerDTO(Playerhand player, String pbfId) {
        PlayerDTO dto = new PlayerDTO();
        dto.setUsername(player.getUsername());
        dto.setPlayerId(player.getPlayerId());
        dto.setPbfId(pbfId);
        return dto;
    }

    private static Playerhand createPlayerHand(Player player, String color, boolean gameCreator) {
        Playerhand playerhand = new Playerhand();
        playerhand.setUsername(player.getUsername());
        playerhand.setPlayerId(player.getId());
        playerhand.setYourTurn(false);
        playerhand.setColor(color);
        playerhand.setGameCreator(gameCreator);
        playerhand.setEmail(player.getEmail());
        return playerhand;
    }

    public void joinGame(String pbfId, Player player, Optional<String> colorOpt) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        pbf.getPlayers().stream().forEach(p -> SendEmail.sendMessage(p.getEmail(), "Game update", player.getUsername() + " joined " + pbf.getName() + ". Go to " + SendEmail.URL + " to find out who!", p.getPlayerId()));
        joinGame(pbf, player.getId(), colorOpt, false);
    }

    /**
     * Joins a game. If it is full it will throw exception
     */
    private void joinGame(PBF pbf, String playerId, Optional<String> colorOpt, boolean gameCreator) {
        if (pbf.getNumOfPlayers() == pbf.getPlayers().size()) {
            log.warn("Cannot join the game. Its full");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageDTO("Cannot join the game. Its full!"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        Player player = playerCollection.findOneById(playerId);

        boolean playerAlreadyJoined = pbf.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(player.getId()));
        if (playerAlreadyJoined) {
            log.warn("Cannot join the game. Player has already joined it");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageDTO("Cannot join the game. You have already joined!"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        player.getGameIds().add(pbf.getId());
        playerCollection.updateById(player.getId(), player);
        Playerhand playerhand;
        if (!pbf.getWithdrawnPlayers().isEmpty()) {
            playerhand = pbf.getWithdrawnPlayers().remove(0);
            playerhand.setEmail(player.getEmail());
            playerhand.setPlayerId(player.getId());
            playerhand.setUsername(player.getUsername());

        } else {
            String color = colorOpt.orElse(chooseColorForPlayer(pbf));
            playerhand = createPlayerHand(player, color, gameCreator);
        }
        if (!pbf.getPlayers().contains(playerhand)) {
            createInfoLog(pbf.getId(), playerhand.getUsername() + " joined the game and is playing color " + playerhand.getColor());
            pbf.getPlayers().add(playerhand);
        }
        pbf = startIfAllPlayers(pbf);
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    private String chooseColorForPlayer(PBF pbf) {
        if (pbf.getPlayers().isEmpty()) {
            return Playerhand.green();
        }

        Set<String> allColors = Sets.newHashSet(Playerhand.green(), Playerhand.purple(), Playerhand.blue(), Playerhand.yellow(), Playerhand.red());

        Set<String> colors = pbf.getPlayers().stream()
                .map(Playerhand::getColor)
                .collect(Collectors.toSet());

        Sets.SetView<String> difference = Sets.difference(allColors, colors);
        return difference.iterator().next();
    }

    public List<PlayerDTO> getAllPlayers(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        PBF pbf = pbfCollection.findOneById(pbfId);
        return pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p, pbf.getId()))
                .collect(toList());
    }

    private PBF startIfAllPlayers(PBF pbf) {
        if (pbf.getPlayers().stream().anyMatch(Playerhand::isYourTurn)) {
            return pbf;
        }
        final int numOfPlayersNeeded = pbf.getNumOfPlayers();
        if (numOfPlayersNeeded == pbf.getPlayers().size()) {
            Collections.shuffle(pbf.getPlayers());
            pbf.getPlayers().get(0).setYourTurn(true);
            createInfoLog(pbf.getId(), "Game has now started. Good luck, and have fun!");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pbf.getPlayers().size(); i++) {
                Playerhand player = pbf.getPlayers().get(i);
                sb.append(getNameForPlayerNumber(i) + " player is " + player.getUsername() + ". ");
            }
            createInfoLog(pbf.getId(), sb.toString());
        }
        return pbf;
    }

    public GameDTO mapGameDTO(PBF pbf, Player player) {
        Preconditions.checkNotNull(pbf);
        //Set common stuff
        GameDTO dto = new GameDTO();
        long created = pbf.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        dto.setCreated(created);
        dto.setId(pbf.getId());
        dto.setType(pbf.getType());
        dto.setName(pbf.getName());
        dto.setWhosTurnIsIt(pbf.getNameOfUsersTurn());
        dto.setActive(pbf.isActive());
        dto.setMapLink(pbf.getMapLink());
        dto.setAssetLink(pbf.getAssetLink());

        //Set logs
        List<GameLog> allPublicLogs = gameLogAction.getGameLogs(pbf.getId());
        List<GameLogDTO> publicGamelogDTOs = allPublicLogs.stream()
                .filter(log -> !Strings.isNullOrEmpty(log.getPublicLog()))
                .map(log -> new GameLogDTO(log.getId(), log.getPublicLog(), log.getCreatedInMillis(), new DrawDTO(log.getDraw())))
                .collect(toList());
        dto.setPublicLogs(publicGamelogDTOs);

        //Set private player info if correct player is loggedIn.
        if (player != null && !Strings.isNullOrEmpty(player.getUsername()) && !Strings.isNullOrEmpty(player.getId())) {
            //Get all the private player stuff
            Optional<Playerhand> playerhand = pbf.getPlayers()
                    .stream()
                    .filter(p -> p.getPlayerId().equals(player.getId()))
                    .findFirst();

            if (playerhand.isPresent()) {
                List<GameLog> allPrivateLogs = gameLogAction.getGameLogsBelongingToPlayer(pbf.getId(), playerhand.get().getUsername());
                List<GameLogDTO> privateGamelogDTOs = allPrivateLogs.stream()
                        .filter(log -> !Strings.isNullOrEmpty(log.getPrivateLog()))
                        .map(log -> new GameLogDTO(log.getId(), log.getPrivateLog(), log.getCreatedInMillis(), new DrawDTO(log.getDraw())))
                        .collect(toList());

                dto.setPlayer(playerhand.get());
                dto.setPrivateLogs(privateGamelogDTOs);
            }
        }
        dto.setRevealedItems(getAllRevealedItems(pbf));
        return dto;
    }

    public boolean withdrawFromGame(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.warn("User with id " + playerId + " is not player of this game, and cannot withdraw");
            Response badReq = Response.status(Response.Status.FORBIDDEN)
                    .entity(new MessageDTO("User is not player of this game, and cannot withdraw"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        Iterator<Playerhand> iterator = pbf.getPlayers().iterator();
        while (iterator.hasNext()) {
            Playerhand playerhand = iterator.next();
            if (playerhand.getPlayerId().equals(playerId)) {
                if (playerhand.isGameCreator()) {
                    Optional<Playerhand> optionalNextPlayer = getRandomPlayerExcept(pbf.getPlayers(), playerhand.getUsername());
                    if (optionalNextPlayer.isPresent()) {
                        Playerhand nextPlayer = optionalNextPlayer.get();
                        nextPlayer.setGameCreator(true);
                        gameLogAction.createCommonPrivatePublicLog("Is now game creator", pbfId, nextPlayer.getPlayerId());
                    } else {
                        Response badReq = Response.status(Response.Status.FORBIDDEN)
                                .entity(new MessageDTO("As game creator, you must end game not withdraw from it"))
                                .build();
                        throw new WebApplicationException(badReq);
                    }
                }
                pbf.getWithdrawnPlayers().add(playerhand);
                iterator.remove();
                gameLogAction.createCommonPublicLog("Withdrew from game", pbfId, playerId);
                //TODO remove from PlayerCollection also
                pbfCollection.updateById(pbf.getId(), pbf);
                return true;
            }
        }

        return false;
    }

    private Optional<Playerhand> getRandomPlayerExcept(List<Playerhand> players, String username) {
        return players.stream().filter(p -> !p.getUsername().equals(username)).findAny();
    }

    @SneakyThrows
    public Chat chat(String pbfId, String message, String username) {
        Chat chat = new Chat();
        chat.setPbfId(pbfId);
        chat.setMessage(URLDecoder.decode(message, "UTF-8"));
        chat.setUsername(username);
        String id = chatCollection.insert(chat).getSavedId();
        chat.setId(id);

        PBF pbf = findPBFById(pbfId);

        if (pbfId != null) {
            String msg = CivSingleton.instance().getChatCache().getIfPresent(pbfId);
            if (Strings.isNullOrEmpty(msg)) {
                CivSingleton.instance().getChatCache().put(pbfId, message);
                pbf.getPlayers()
                        .stream()
                        .filter(p -> !p.getUsername().equals(username))
                        .forEach(p -> {
                                    if (CivUtil.shouldSendEmailInGame(p)) {
                                        SendEmail.sendMessage(p.getEmail(), "New Chat", username + " wrote in the chat: " + chat.getMessage()
                                                + ".\nLogin to " + SendEmail.gamelink(pbfId) + " to see the chat", p.getPlayerId());
                                        pbfCollection.updateById(pbfId, pbf);
                                    }
                                }
                        );
            }
        }

        return chat;
    }

    public List<ChatDTO> getChat(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        List<Chat> chats = chatCollection.find(DBQuery.is("pbfId", pbfId)).sort(DBSort.desc("created")).toArray();

        PBF pbf = findPBFById(pbfId);
        Map<String, String> colorMap = pbf.getPlayers().stream()
                .collect(Collectors.toMap(Playerhand::getUsername, (playerhand) -> {
                    return (playerhand.getColor() != null) ? playerhand.getColor() : "";
                }));

        List<ChatDTO> chatDTOs = new ArrayList<>(chats.size());
        for (Chat c : chats) {
            chatDTOs.add(new ChatDTO(c.getId(), c.getPbfId(), c.getUsername(), c.getMessage(), colorMap.get(c.getUsername()), c.getCreatedInMillis()));
        }

        //Sort newest date first
        Collections.sort(chatDTOs, (o1, o2) -> -Long.valueOf(o1.getCreated()).compareTo(o2.getCreated()));
        return chatDTOs;
    }

    public void endGame(String pbfId, Player player, String winner) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        if (!"admin".equals(player.getUsername())) {
            Playerhand playerhand = getPlayerhandByPlayerId(player.getId(), pbf);
            //Only game creator can end game
            if (!playerhand.isGameCreator()) {
                Response response = Response.status(Response.Status.FORBIDDEN)
                        .entity(new MessageDTO("Only game creator can end game"))
                        .build();
                throw new WebApplicationException(response);
            }
        }

        if (!Strings.isNullOrEmpty(winner)) {
            if (!pbf.getPlayers().stream()
                    .anyMatch(p -> p.getUsername().equals(winner))) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            createInfoLog(pbfId, winner + " won the game! Congratulations!");
            pbf.setWinner(winner);
        }

        pbf.setActive(false);
        createInfoLog(pbfId, player.getUsername() + " Ended this game");
        createInfoLog(pbfId, "Thank you for playing! Please donate if you liked this game!");
        pbf.getPlayers().forEach(p -> SendEmail.sendMessage(p.getEmail(), "Game ended", pbf.getName() + " has ended. I hope you enjoyed playing.\n" +
                "If you like this game, please consider donating. You can find the link at the bottom of the site. It will help keep the lights on, and continue adding more features!" +
                "\n\nBest regards Shervin Asgari aka Cash", p.getPlayerId()));
        pbfCollection.updateById(pbfId, pbf);
    }

    /**
     * Will return the id from the google presentation
     */
    @SneakyThrows
    public String addMapLink(String pbfId, String linkEncoded, String playerId) {
        String link = URLDecoder.decode(linkEncoded, "UTF-8");
        if (link.matches("(?i)^https://docs\\.google\\.com/presentation/d/.*$")) {
            String removedPart = link.replace("https://docs.google.com/presentation/d/", "");
            String id = removedPart.split("/")[0];
            log.info("Id from google presentation is: " + id);

            PBF pbf = pbfCollection.findOneById(pbfId);

            pbf.setMapLink(id);
            pbfCollection.updateById(pbfId, pbf);
            Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
            createInfoLog(pbfId, playerhand.getUsername() + " Added map link");
            return id;
        }
        return null;
    }

    /**
     * Will return the id from the google presentation
     */
    @SneakyThrows
    public String addAssetLink(String pbfId, String linkEncoded, String playerId) {
        String link = URLDecoder.decode(linkEncoded, "UTF-8");
        if (link.matches("(?i)^https://docs\\.google\\.com/spreadsheets/d/.*$")) {
            String removedPart = link.replace("https://docs.google.com/spreadsheets/d/", "");
            String id = removedPart.split("/")[0];
            log.info("Id from google presentation is: " + id);

            PBF pbf = pbfCollection.findOneById(pbfId);
            pbf.setAssetLink(id);
            pbfCollection.updateById(pbfId, pbf);
            Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
            createInfoLog(pbfId, playerhand.getUsername() + " Added asset link");
            return id;
        }
        return null;
    }

    public void changeUserFromExistingGame(String gameid, String oldUsername, String newUsername) {
        Preconditions.checkNotNull(gameid);
        Preconditions.checkNotNull(oldUsername);
        Preconditions.checkNotNull(newUsername);

        PBF pbf = pbfCollection.findOneById(gameid);
        Player toPlayer = playerCollection.find(DBQuery.is("username", newUsername)).toArray(1).get(0);

        //Find all instance of ownerid, and replace with newUsername
        Playerhand playerhandToReplace = pbf.getPlayers().stream().filter(p -> p.getUsername().equals(oldUsername)).findFirst().orElseThrow(PlayerAction::cannotFindPlayer);

        playerhandToReplace.setUsername(newUsername);
        playerhandToReplace.setPlayerId(toPlayer.getId());
        playerhandToReplace.setEmail(toPlayer.getEmail());

        playerhandToReplace.getBarbarians().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getBattlehand().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getTechsChosen().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getItems().forEach(b -> b.setOwnerId(toPlayer.getId()));

        pbfCollection.updateById(pbf.getId(), pbf);
        createInfoLog(pbf.getId(), newUsername + " is now playing instead of " + oldUsername);
        SendEmail.sendMessage(playerhandToReplace.getEmail(), "You are now playing in " + pbf.getName(), "Please log in to http://playciv.com and start playing!", playerhandToReplace.getPlayerId());
    }

    public boolean deleteGame(String gameid) {
        Preconditions.checkNotNull(gameid);

        final PBF pbf = findPBFById(gameid);
        WriteResult<PBF, String> writeResult = pbfCollection.removeById(gameid);
        log.warn("Managed to delete game: " + Strings.isNullOrEmpty(writeResult.getWriteResult().toString()));

        List<Player> playerList = playerCollection.find().toArray().stream()
                .filter(p -> p.getGameIds().contains(gameid))
                .collect(toList());

        playerList.forEach(player -> {
            log.info("Deleting game from " + player.getUsername() + "s collection also");
            player.getGameIds().remove(gameid);
            SendEmail.sendMessage(player.getEmail(), "Game deleted", "Your game " + pbf.getName() + " was deleted by the admin. " +
                    "If this was incorrect, please contact the admin.", player.getId());
            playerCollection.save(player);
        });

        return true;
    }

    public void sendMailToAll(String msg) {
        playerCollection.find().toArray()
                .stream()
                .filter(p -> !p.isDisableEmail())
                .forEach(player -> {
                    SendEmail.sendMessage(player.getEmail(), "Update to civilization (playciv.com)",
                            "Hello " + player.getUsername() +
                                    "\n" + msg, player.getId());
                });
    }

    /**
     * Gets public chat which is 1 week old and maximum 50 entries, sorted on created
     */
    public List<ChatDTO> getPublicChat() {
        return chatCollection.find(DBQuery.notExists("pbfId")).sort(DBSort.desc("created")).toArray()
                .stream()
                .filter(c -> c.getCreated().isAfter(LocalDateTime.now().minusWeeks(1)))
                .sorted((a, b) -> a.getCreated().compareTo(b.getCreated()))
                .map(c -> new ChatDTO(c.getUsername(), c.getMessage(), c.getCreatedInMillis()))
                .limit(50)
                .collect(toList());
    }

    public List<WinnerDTO> getWinners() {
        List<PBF> pbfs = pbfCollection.find().toArray();
        final ListMultimap<String, String> multimap = ArrayListMultimap.create();

        pbfs.stream()
                .filter(pbf -> !Strings.isNullOrEmpty(pbf.getWinner()))
                .forEach(pbf -> multimap.put(pbf.getWinner(), pbf.getId()));

        return multimap.keySet().stream()
                .map(user -> new WinnerDTO(user, multimap.get(user).size()))
                .collect(toList());
    }

    private List<Item> getAllRevealedItems(PBF pbf) {
        //Had to have comparator inside sort, otherwise weird exception
        Stream<Item> discardedStream = pbf.getDiscardedItems().stream()
                .sorted((o1, o2) -> o1.getSheetName().compareTo(o2.getSheetName()));

        Stream<Item> playerStream = pbf.getPlayers().stream()
                .flatMap(p -> p.getItems().stream())
                .filter(it -> !it.isHidden())
                .sorted((o1, o2) -> o1.getSheetName().compareTo(o2.getSheetName()));

        Stream<Item> concatedStream = Stream.concat(discardedStream, playerStream);
        return concatedStream.collect(toList());
    }

    private String getNameForPlayerNumber(int nr) {
        switch (nr) {
            case 0:
                return "First";
            case 1:
                return "Second";
            case 2:
                return "Third";
            case 3:
                return "Fourth";
            case 4:
                return "Fifth";
            case 5:
                return "Sixth";
        }

        return "";
    }

    public boolean disableEmailForPlayer(String playerId) {
        Player player = playerCollection.findOneById(playerId);
        if (player != null && !Strings.isNullOrEmpty(player.getNewPassword())) {
            log.warn("Player " + player.getEmail() + " no longer wants email");
            player.setDisableEmail(true);
            playerCollection.updateById(playerId, player);
            return true;
        }
        return false;
    }
}
