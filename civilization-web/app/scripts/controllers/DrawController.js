'use strict';
(function (module) {
  var DrawController = function ($log, GameService, DrawService, currentUser, Util, growl, $routeParams, $scope) {
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
      model.number = 3;
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
      DrawService.drawUnitsFromHand($routeParams.id, model.number);
    };

    model.drawBarbarians = function() {
      DrawService.drawBarbarians($routeParams.id);
    };

    model.discardBarbarians = function() {
      DrawService.discardBarbarians($routeParams.id)
        .then(function() {
          model.barbarians = [];
        });
    };

    model.nextElement = function(obj) {
      return Util.nextElement(obj);
    };

    model.revealBattlehand = function() {
      if(model.battlehand && model.battlehand.length > 0) {
        DrawService.revealHand($routeParams.id);
      }
    };

    initialize();
  };

  module.controller("DrawController",
    ["$log", "GameService", "DrawService", "currentUser", "Util", "growl", "$routeParams", "$scope", DrawController]);

}(angular.module("civApp")));
