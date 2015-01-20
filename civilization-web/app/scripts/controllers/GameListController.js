'use strict';
(function (module) {
  var GameListController = function (games, $log, GameService, currentUser) {
    var model = this;
    model.user = currentUser.profile;

    model.isUserPlaying = function(players) {
      if(players) {
        for(var i = 0; i < players.length; i++) {
          var player = players[i];
          if(player && player.username === model.user.username) {
            return true;
          }
        }
      }
      return false;
    };


    model.joinGame = function (game) {
      var joinPromise = GameService.joinGame(game)
        .then(function (game) {
          model.game = game;
          $scope.userHasAccess = game.player && game.player.username === model.user.username;
          model.yourTurn = game.player && game.player.yourTurn;
          return game;
        });

      $log.debug("User wants to join game with nr " + game.id);
      return joinPromise;
    };

    var initialize = function () {
      model.games = games;
      $log.info("Got games");
    };

    initialize();
  };

  module.controller("GameListController",
    ["games", "$log", "GameService", "currentUser", GameListController]);

}(angular.module("civApp")));
