'use strict';
(function (module) {
  var GameListController = function ($log, GameService) {
    var model = this;

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
    ["$log", "GameService", GameListController]);

}(angular.module("civApp")));
