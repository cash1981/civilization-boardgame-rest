'use strict';
(function (module) {
  var GameListController = function (games, $log, GameService, currentUser, $modal) {
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

    model.openCreateNewGame = function(size) {
      var modalInstance = $modal.open({
        templateUrl: 'createNewGame.html',
        controller: 'RegisterController as registerCtrl',
        size: size
      });

      modalInstance.result.then(function(game) {
        if(game) {
          $log.info(game.name);
          $log.info(game.type);
          $log.info(game.numOfPlayers);
          $log.info(game.color);
          GameService.createGame(game);
        }
      }, function () {
        //Cancel callback here
      });
    };

    var initialize = function () {
      model.user = currentUser.profile;
      model.games = games;
      $log.info("Got games");
    };

    initialize();
  };

  module.controller("GameListController",
    ["games", "$log", "GameService", "currentUser", "$modal", GameListController]);

}(angular.module("civApp")));
