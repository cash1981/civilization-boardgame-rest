'use strict';
(function (module) {
  var BattleController = function ($log, GameService, PlayerService, currentUser, growl, $routeParams) {
    var model = this;

    var initialize = function() {
      model.user = currentUser.profile;
      model.number = 3;
      model.barbarians = [];
      model.drawnUnits = [];
    };

    model.drawUnits = function() {
      $log.info("Draw " + model.number + " units");
      if(model.number < 3) {
        growl.error("You cannot draw less than 3 units");
        return;
      }
      PlayerService.drawUnits($routeParams.id)
        .then(function(data) {
          model.drawnUnits = data;
        });
    };

    model.drawBarbarians = function() {
      PlayerService.drawBarbarians($routeParams.id)
        .then(function(barbs) {
          model.barbarians = barbs;
        });
    };

    model.discardBarbarians = function() {
      PlayerService.discardBarbarians($routeParams.id)
        .then(function() {
          model.barbarians = [];
        });
    };

    initialize();
  };

  module.controller("BattleController",
    ["$log", "GameService", "PlayerService", "currentUser", "growl", "$routeParams", BattleController]);

}(angular.module("civApp")));
