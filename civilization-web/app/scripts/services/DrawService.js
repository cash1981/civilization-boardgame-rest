'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function ($http, $q, $log, growl, currentUser, BASE_URL, GameService, Util) {
    var baseUrl = BASE_URL + "/draw/";

    var revealBattlehand = function(gameid) {

    };
    return {
      revealBattlehand: revealBattlehand
    };

  });

}(angular.module("civApp")));
