'use strict';
(function (module) {
  var GameController = function ($log, $routeParams, GameService) {
    var model = this;

    $log.info("Inside Game");
    GameService.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
      });

  };

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", GameController]);

}(angular.module("civApp")));
