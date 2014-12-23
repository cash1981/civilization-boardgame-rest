'use strict';
(function (module) {
  var GameListController = function ($log, GameService, currentUser) {
    var model = this;
    model.user = currentUser.profile;

    model.joinGame = function (game) {
      $log.debug("User wants to join game with nr " + game.id);
      //TODO call PUT on /game/{pbfId}
    };

    var loadGames = function (game) {
      model.games = game;
      $log.info("Got games");
    };

    var error = function (error) {
      $log.info("Got error loading games");
      if (error.data) {
        model.errorMessage = error.data.message;
      } else {
        model.errorMessage = "Unknown error";
      }
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
