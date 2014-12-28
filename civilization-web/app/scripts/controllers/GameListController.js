'use strict';
(function (module) {
  var GameListController = function (games, $log, gameData, currentUser) {
    var model = this;
    model.user = currentUser.profile;

    model.isUserPlaying = function(players) {
      if(players) {
        for(var i = 0; i < players.length; i++) {
          var player = players[i];
          if(player && player.username == model.user.username) {
            return true;
          }
        }
      }
      return false;
    };


    model.joinGame = function (game) {
      var joinPromise = gameData.joinGame(game)
        .then(function (game) {
          model.game = game;
          $scope.userHasAccess = game.player && game.player.username === model.user.username;
          model.yourTurn = game.player && game.player.yourTurn;
          return game;
        });

      $log.debug("User wants to join game with nr " + game.id);
      return joinPromise;
    };

    var error = function (error) {
      $log.info("Got error loading games");
      if (error.data) {
        model.errorMessage = error.data.message;
      } else {
        model.errorMessage = "Unknown error";
      }
    };


    var initialize = function () {
      model.games = games;
      $log.info("Got games");
    };

    initialize();
  };

  module.controller("GameListController",
    ["games", "$log", "gameData", "currentUser", GameListController]);

}(angular.module("civApp")));
