'use strict';
(function (module) {
  var DrawController = function ($log, GameService, PlayerService, currentUser, Util, growl, $routeParams, $scope) {
    var model = this;

    $scope.$watch(function () {
      return GameService.getGameById($routeParams.id);
    }, function (newVal) {
      if (!newVal) {
        return;
      }
      var game = newVal;
      model.barbarians = game.player.barbarians;
      model.battlehand = game.player.battlehand;
    });

    var initialize = function() {
      model.user = currentUser.profile;
      model.number = 1;
      if($scope.currentGame.player.barbarians) {
        model.barbarians = $scope.currentGame.player.barbarians;
      } else {
        model.barbarians = [];
      }

      if($scope.currentGame.player.battlehand) {
        model.battlehand = $scope.currentGame.player.battlehand;
      } else {
        model.battlehand = [];
      }
    };

    model.drawUnits = function() {
      $log.info("Draw " + model.number + " units");
      if(model.number < 1) {
        growl.error("You must draw at least 1 unit");
        return;
      }
      PlayerService.drawUnitsForBattle($routeParams.id, model.number);
    };

    model.drawBarbarians = function() {
      PlayerService.drawBarbarians($routeParams.id);
    };

    model.discardBarbarians = function() {
      PlayerService.discardBarbarians($routeParams.id)
        .then(function() {
          model.barbarians = [];
        });
    };

    model.nextElement = function(obj) {
      return Util.nextElement(obj);
    };

    model.revealBattlehand = function() {
      if(model.battlehand) {

      }
    };

    initialize();
  };

  module.controller("DrawController",
    ["$log", "GameService", "PlayerService", "currentUser", "Util", "growl", "$routeParams", "$scope", DrawController]);

}(angular.module("civApp")));
