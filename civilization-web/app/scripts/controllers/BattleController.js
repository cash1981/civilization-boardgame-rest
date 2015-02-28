'use strict';
(function (module) {
  var BattleController = function ($log, GameService, PlayerService, currentUser, growl) {
    var model = this;

    var initialize = function() {
      model.user = currentUser.profile;
      model.number = 3;
    };

    model.drawUnits = function() {
      $log.info("Draw " + model.number + " units");
      if(model.number < 3) {
        growl.error("You cannot draw less than 3 units");
        return;
      }
    };

    initialize();
  };

  module.controller("BattleController",
    ["$log", "GameService", "PlayerService", "currentUser", "growl", BattleController]);

}(angular.module("civApp")));
