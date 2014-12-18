'use strict';
(function (module) {
  var GameListController = function ($log, GameService, currentUser) {
    var model = this;
    model.user = currentUser.profile;

    model.joinGame = function(game) {
      $log.info("User wants to join game with nr " + game.id);
      //TODO call PUT on /game/{pbfId}
    };

    var loadGames = function(game) {
      model.games = game;
      $log.info("Got games");
    };

    var error = function(error) {
      $log.info("Got error loading games");
      model.errorMessage = error.data.message;
    };

    function initialize() {
      GameService.getAllGames()
        .then(loadGames)
        .catch(error);
    }

    initialize();
  };

  module.controller("GameListController",
    ["$log", "GameService", "currentUser", GameListController]);

}(angular.module("civApp")));
