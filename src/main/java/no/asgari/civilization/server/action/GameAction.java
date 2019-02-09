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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.*;
import no.asgari.civilization.server.email.SendEmail;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.exception.BadRequestException;
import no.asgari.civilization.server.exception.ForbiddenException;
import no.asgari.civilization.server.exception.NotFoundException;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class GameAction extends BaseAction {
    private final GameLogAction gameLogAction;
    private final EmailAction emailAction;

    @Autowired
    public GameAction(GameLogAction gameLogAction, EmailAction emailAction) {
        this.gameLogAction = gameLogAction;
        this.emailAction = emailAction;
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

    public String createNewGame(CreateNewGameDTO dto, String playerId) {
        PBF pbf = new PBF();
        pbf.setName(dto.getName());
        pbf.setType(dto.getType());
        pbf.setNumOfPlayers(dto.getNumOfPlayers());
        ItemReader itemReader = new ItemReader();
        readItemFromExcel(dto.getType(), itemReader);

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

        Collections.shuffle(pbf.getItems(), new Random(System.nanoTime()));
        Collections.shuffle(pbf.getTechs(), new Random(System.nanoTime()));
        Collections.shuffle(pbf.getSocialPolicies(), new Random(System.nanoTime()));

        pbf.getItems().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));
        pbf.getTechs().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));
        pbf.getSocialPolicies().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));

        pbf = pbfRepository.save(pbf);
        log.info("PBF game created with id " + pbf.getId());
        joinGame(pbf, playerId, Optional.of(dto.getColor()), true);

        emailAction.sendEmailNewGameCreated(pbf.getName());
        return pbf.getId();
    }


    private void readItemFromExcel(GameType gameType, ItemReader itemReader) {
        try {
            itemReader.readItemsFromExcel(gameType);
            if (!CivSingleton.instance().itemsCache().containsKey(gameType)) {
                CivSingleton.instance().itemsCache().put(gameType, itemReader);
            }
        } catch (IOException e) {
            log.error("Couldn't read Excel document " + e.getMessage(), e);
        }
    }

    /**
     * Returns all games sorted on active first
     *
     * @return
     */
    public List<PbfDTO> getAllGames() {
        List<PBF> pbfs = pbfRepository.findAll();
        return pbfs.stream()
                .map(GameAction::createPbfDTO)
                .sorted((o1, o2) -> {
                    int v = Boolean.valueOf(o1.isActive()).compareTo(o2.isActive());
                    if (v != 0) return v;
                    return Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                })
                .collect(toList());
    }

    public void joinGame(String pbfId, Player player, Optional<String> colorOpt) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);

        joinGame(pbf, player.getId(), colorOpt, false);

        Thread thread = new Thread(() -> {
            pbf.getPlayers().stream()
                    .filter(p -> !p.getPlayerId().equals(player.getId()))
                    .forEach(p -> SendEmail.sendMessage(p.getEmail(), "Game update", player.getUsername() + " joined " + pbf.getName() + ". Go to " + SendEmail.URL + " to find out who!", p.getPlayerId()));
        });
        thread.start();
    }

    /**
     * Joins a game. If it is full it will throw exception
     */
    private void joinGame(PBF pbf, String playerId, Optional<String> colorOpt, boolean gameCreator) {
        if (pbf.getNumOfPlayers() == pbf.getPlayers().size()) {
            log.warn("Cannot join the game. Its full");
            throw new BadRequestException();
        }

        Player player = playerRepository.findById(playerId).orElseThrow(NotFoundException::new);

        boolean playerAlreadyJoined = pbf.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(playerId));
        if (playerAlreadyJoined) {
            log.warn("Cannot join the game. Player has already joined it");
            throw new ForbiddenException();
        }

        player.getGameIds().add(pbf.getId());
        player = playerRepository.save(player);
        Playerhand playerhand;
        if (!pbf.getWithdrawnPlayers().isEmpty()) {
            playerhand = pbf.getWithdrawnPlayers().remove(0);
            boolean updated = gameLogAction.updateGameLog(pbf.getId(), playerhand.getUsername(), player.getUsername());
            log.info("Managed to update gameLog: " + updated);
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
        pbfRepository.save(pbf);
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
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        return pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p, pbf.getId()))
                .sorted(Comparator.comparing(PlayerDTO::getUsername))
                .collect(toList());
    }

    private PBF startIfAllPlayers(PBF pbf) {
        if (pbf.getPlayers().stream().anyMatch(Playerhand::isYourTurn)) {
            return pbf;
        }
        final int numOfPlayersNeeded = pbf.getNumOfPlayers();
        if (numOfPlayersNeeded == pbf.getPlayers().size()) {
            Collections.shuffle(pbf.getPlayers());
            Playerhand firstplayer = pbf.getPlayers().get(0);
            firstplayer.setYourTurn(true);
            firstplayer.setPlayernumber(1);

            createInfoLog(pbf.getId(), "Game has now started. Good luck, and have fun!");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pbf.getPlayers().size(); i++) {
                Playerhand player = pbf.getPlayers().get(i);
                player.setPlayernumber(i + 1);
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
                .sorted(Comparator.comparing(GameLogDTO::getCreated))
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
                        .sorted(Comparator.comparing(GameLogDTO::getCreated))
                        .collect(toList());

                dto.setPlayer(playerhand.get());
                dto.setPrivateLogs(privateGamelogDTOs);
            }
        }
        dto.setRevealedItems(getAllRevealedItems(pbf));
        return dto;
    }

    public boolean withdrawFromGame(String pbfId, String playerId) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);

        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.warn("User with id " + playerId + " is not player of this game, and cannot withdraw");
            throw new ForbiddenException();
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
                        throw new ForbiddenException();
                    }
                }
                pbf.getWithdrawnPlayers().add(playerhand);
                iterator.remove();
                gameLogAction.createCommonPublicLog("withdrew from game", pbfId, playerId);
                //TODO remove from PlayerCollection also
                pbfRepository.save(pbf);
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
        chat.setMessage(URLDecoder.decode(message, StandardCharsets.UTF_8));
        chat.setUsername(username);
        chatRepository.save(chat);

        emailAction.sendChat(pbfId, message, username, chat.getMessage());

        return chat;
    }

    public List<ChatDTO> getChat(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        List<Chat> chats = chatRepository.findAllByPbfIdOrderByCreated(pbfId);
        if (chats == null) {
            return new ArrayList<>();
        }
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
        chatDTOs.sort((o1, o2) -> -Long.compare(o1.getCreated(), o2.getCreated()));
        return chatDTOs;
    }

    public void endGame(String pbfId, Player player, String winner) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        if (!"admin".equals(player.getUsername())) {
            Playerhand playerhand = getPlayerhandByPlayerId(player.getId(), pbf);
            //Only game creator can end game
            if (!playerhand.isGameCreator()) {
                throw new ForbiddenException();
            }
        }

        if (!Strings.isNullOrEmpty(winner)) {
            if (!pbf.getPlayers().stream()
                    .anyMatch(p -> p.getUsername().equals(winner))) {
                throw new NotFoundException();
            }
            createInfoLog(pbfId, winner + " won the game! Congratulations!");
            pbf.setWinner(winner);
        }

        pbf.setActive(false);
        createInfoLog(pbfId, player.getUsername() + " Ended this game");
        createInfoLog(pbfId, "Thank you for playing! Please donate if you liked this game!");
        pbf = pbfRepository.save(pbf);

        emailAction.gameEnd(pbf);
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

            PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);

            pbf.setMapLink(id);
            pbf = pbfRepository.save(pbf);
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

            PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
            pbf.setAssetLink(id);
            pbf = pbfRepository.save(pbf);
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

        PBF pbf = pbfRepository.findById(gameid).orElseThrow(NotFoundException::new);
        Player toPlayer = playerRepository.findOneByUsername(newUsername);

        //Find all instance of ownerid, and replace with newUsername
        Playerhand playerhandToReplace = pbf.getPlayers().stream().filter(p -> p.getUsername().equals(oldUsername)).findFirst().orElseThrow(PlayerAction::cannotFindPlayer);

        playerhandToReplace.setUsername(newUsername);
        playerhandToReplace.setPlayerId(toPlayer.getId());
        playerhandToReplace.setEmail(toPlayer.getEmail());

        playerhandToReplace.getBarbarians().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getBattlehand().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getTechsChosen().forEach(b -> b.setOwnerId(toPlayer.getId()));
        playerhandToReplace.getItems().forEach(b -> b.setOwnerId(toPlayer.getId()));

        pbfRepository.save(pbf);
        createInfoLog(pbf.getId(), newUsername + " is now playing instead of " + oldUsername);
        SendEmail.sendMessage(playerhandToReplace.getEmail(), "You are now playing in " + pbf.getName(), "Please log in to http://playciv.com and start playing!", playerhandToReplace.getPlayerId());
    }

    public boolean deleteGame(String gameid) {
        Preconditions.checkNotNull(gameid);

        PBF pbf = findPBFById(gameid);
        pbfRepository.deleteById(gameid);

        List<Player> playerList = playerRepository.findAll().stream()
                .filter(p -> p.getGameIds().contains(gameid))
                .collect(toList());

        playerList.forEach(player -> {
            log.info("Deleting game from " + player.getUsername() + "s collection also");
            player.getGameIds().remove(gameid);
            SendEmail.sendMessage(player.getEmail(), "Game deleted", "Your game " + pbf.getName() + " was deleted by the admin. " +
                    "If this was incorrect, please contact the admin.", player.getId());
            playerRepository.save(player);
        });

        return true;
    }

    public void sendMailToAll(String msg) {
        playerRepository.findAll()
                .parallelStream()
                .filter(p -> !p.isDisableEmail())
                .forEach(player -> {
                    SendEmail.sendMessage(player.getEmail(), "Message from cash at playciv.com",
                            "Hello " + player.getUsername() +
                                    "\n" + msg, player.getId());
                });
    }

    /**
     * Gets public chat which is 1 month old and maximum 50 entries, sorted on created
     */
    public List<ChatDTO> getPublicChat() {
        return chatRepository.findTop50ByPbfIdIsNullOrderByCreated()
                .stream()
                .filter(c -> c.getCreated().isAfter(LocalDateTime.now().minusMonths(1)))
                .sorted(Comparator.comparing(Chat::getCreated))
                .map(c -> new ChatDTO(c.getUsername(), c.getMessage(), c.getCreatedInMillis()))
                .limit(50)
                .collect(toList());
    }

    public List<CivWinnerDTO> getCivHighscore() {
        if (!CivSingleton.instance().itemsCache().containsKey(GameType.WAW)) {
            readItemFromExcel(GameType.WAW, new ItemReader());
        }

        ItemReader itemReader = CivSingleton.instance().itemsCache().get(GameType.WAW);
        if (itemReader == null) {
            return Collections.emptyList();
        }

        List<PBF> pbfs = pbfRepository.findAll();

        try {
            Map<String, Long> numberOfCivsWinning = pbfs.stream()
                    .filter(pbf -> !Strings.isNullOrEmpty(pbf.getWinner()))
                    .filter(pbf -> !pbf.isActive())
                    .filter(pbf -> {
                        String playerWhoWon = pbf.getWinner();
                        return pbf.getPlayers().stream()
                                .filter(p -> p.getUsername().equals(playerWhoWon))
                                .anyMatch(p -> p.getCivilization() != null);
                    })
                    .map(pbf -> {
                        String playerWhoWon = pbf.getWinner();
                        Playerhand playerhand = pbf.getPlayers().stream()
                                .filter(p -> p.getUsername().equals(playerWhoWon))
                                .filter(p -> p.getCivilization() != null)
                                .findFirst()
                                .get();
                        return playerhand.getCivilization().getName();
                    })
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            Map<String, Long> numberOfCivAttempts = pbfs.stream()
                    .filter(pbf -> !Strings.isNullOrEmpty(pbf.getWinner()))
                    .filter(pbf -> !pbf.isActive())
                    .flatMap(pbf -> pbf.getPlayers().stream())
                    .filter(p -> p.getCivilization() != null)
                    .map(p -> p.getCivilization().getName())
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            return itemReader.shuffledCivs.stream()
                    .map(civ -> new CivWinnerDTO(civ.getName(), numberOfCivsWinning.get(civ.getName()), numberOfCivAttempts.get(civ.getName())))
                    .sorted()
                    .collect(toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Item> getAllRevealedItems(PBF pbf) {
        //Had to have comparator inside sort, otherwise weird exception
        Stream<Item> discardedStream = pbf.getDiscardedItems().stream()
                .sorted(Comparator.comparing(Spreadsheet::getSheetName));

        Stream<Item> playerStream = pbf.getPlayers().stream()
                .flatMap(p -> p.getItems().stream())
                .filter(it -> !it.isHidden())
                .sorted(Comparator.comparing(Spreadsheet::getSheetName));

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
        Preconditions.checkNotNull(playerId);
        Player player = playerRepository.findById(playerId).orElseThrow(NotFoundException::new);
        log.warn("Player " + player.getEmail() + " no longer wants email");
        player.setDisableEmail(true);
        playerRepository.save(player);
        return true;
    }

    public boolean startEmailForPlayer(String playerId) {
        Preconditions.checkNotNull(playerId);
        Player player = playerRepository.findById(playerId).orElseThrow(NotFoundException::new);
        log.warn("Player " + player.getEmail() + " no longer wants email");
        player.setDisableEmail(false);
        playerRepository.save(player);
        return true;
    }

    public PlayerHighscoreDTO getPlayerHighScore() {
        ListMultimap<String, Integer> winnersByNumOfPlayers = ArrayListMultimap.create();
        PlayerHighscoreDTO dto = new PlayerHighscoreDTO();
        List<Player> allPlayers = playerRepository.findAll();
        dto.setTotalNumberOfPlayers(allPlayers.size());
        //key == username, value = num of players

        List<PBF> finishedGames = pbfRepository.findAll().stream()
                .filter(pbf -> !pbf.isActive())
                .filter(pbf -> !Strings.isNullOrEmpty(pbf.getWinner()))
                .collect(toList());
        dto.setTotalNumberOfGames(finishedGames.size());

        finishedGames.forEach(pbf -> winnersByNumOfPlayers.put(pbf.getWinner(), pbf.getNumOfPlayers()));
        dto.setWinners(getAllWinners(winnersByNumOfPlayers, allPlayers, finishedGames));
        dto.setFiveWinners(getWinners(winnersByNumOfPlayers, finishedGames, 5));
        dto.setFourWinners(getWinners(winnersByNumOfPlayers, finishedGames, 4));
        dto.setThreeWinners(getWinners(winnersByNumOfPlayers, finishedGames, 3));
        dto.setTwoWinners(getWinners(winnersByNumOfPlayers, finishedGames, 2));

        dto.setFivePlayerGamesTotal(finishedGames.stream().filter(pbf -> pbf.getNumOfPlayers() == 5).count());
        dto.setFourPlayerGamesTotal(finishedGames.stream().filter(pbf -> pbf.getNumOfPlayers() == 4).count());
        dto.setThreePlayerGamesTotal(finishedGames.stream().filter(pbf -> pbf.getNumOfPlayers() == 3).count());
        dto.setTwoPlayerGamesTotal(finishedGames.stream().filter(pbf -> pbf.getNumOfPlayers() == 2).count());
        return dto;
    }

    private List<WinnerDTO> getAllWinners(ListMultimap<String, Integer> winnersByNumOfPlayers, List<Player> allPlayers, List<PBF> finishedGames) {
        Map<String, Long> attemptsPerUserAllGames = finishedGames.stream()
                .flatMap(pbf -> pbf.getPlayers().stream())
                .map(Playerhand::getUsername)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));


        List<WinnerDTO> filteredPlayers = allPlayers.stream()
                .filter(p -> !winnersByNumOfPlayers.containsKey(p.getUsername()) && p.getUsername() != null)
                .map(p -> {
                    long attempts = attemptsPerUserAllGames.get(p.getUsername()) == null ? 0L : attemptsPerUserAllGames.get(p.getUsername());
                    return new WinnerDTO(p.getUsername(), 0, attempts);
                })
                .collect(toList());


        List<WinnerDTO> allWinners = winnersByNumOfPlayers.keySet().stream()
                .map(user -> {
                    Long attempts = attemptsPerUserAllGames.get(user);
                    return new WinnerDTO(user, winnersByNumOfPlayers.get(user).size(), attempts == null ? 0L : attempts);
                })
                .collect(toList());

        allWinners.addAll(filteredPlayers);
        Collections.sort(allWinners);
        Collections.reverse(allWinners);
        return allWinners;
    }

    private List<WinnerDTO> getWinners(ListMultimap<String, Integer> winnersByNumOfPlayers, List<PBF> finishedGames, int numOfPlayers) {
        Map<String, Long> attemptsPerUser = attemptsPerUser(finishedGames, numOfPlayers);
        return finishedGames.stream()
                .filter(pbf -> pbf.getNumOfPlayers() == numOfPlayers)
                .flatMap(pbf -> pbf.getPlayers().stream())
                .distinct()
                //.filter(p -> !winnersByNumOfPlayers.containsKey(p.getUsername()) && p.getUsername() != null)
                .map(p -> {
                    List<Integer> winner = winnersByNumOfPlayers.get(p.getUsername());
                    int totalWins = 0;
                    long attempts = attemptsPerUser.get(p.getUsername());
                    if (winner != null && !winner.isEmpty()) {
                        totalWins = (int) winner
                                .stream()
                                .filter(num -> num == numOfPlayers)
                                .count();
                    }
                    return new WinnerDTO(p.getUsername(), totalWins, attempts);
                })
                .sorted(((o1, o2) -> o2.compareTo(o1)))
                .collect(toList());
    }

    private Map<String, Long> attemptsPerUser(List<PBF> finishedGames, int numOfPlayers) {
        return finishedGames.stream()
                .filter(pbf -> pbf.getNumOfPlayers() == numOfPlayers)
                .flatMap(pbf -> pbf.getPlayers().stream())
                .map(Playerhand::getUsername)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    public void takeTurn(String gameid, String fromUsername) {
        PBF pbf = findPBFById(gameid);
        Optional<Playerhand> player = pbf.getPlayers().stream().filter(p -> p.getUsername().equals(fromUsername)).findFirst();

        if (player.isPresent()) {
            Playerhand playerTurn = pbf.getPlayers().stream().filter(Playerhand::isYourTurn).findFirst().get();
            playerTurn.setYourTurn(false);

            player.get().setYourTurn(true);
            pbfRepository.save(pbf);
            return;
        }
        throw new NotFoundException("Player not found");

    }
}
