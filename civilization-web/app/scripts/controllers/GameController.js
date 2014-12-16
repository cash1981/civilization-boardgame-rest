//"use strict";
(function (module) {
  var GameListController = function ($log, $routeParams, GameService) {
    var model = this;

    GameService.getGameById($routeParams.id)
      .then(function(game) {
        model.game = game;
      });

  };

  module.controller("GameListController",
    ["$log", "$routeParams", "GameService", GameListController]);

}(angular.module("civApp")));
