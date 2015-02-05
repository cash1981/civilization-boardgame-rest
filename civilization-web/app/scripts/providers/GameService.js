'use strict';
(function (civApp) {

  civApp.config(function ($provide) {
    $provide.provider("GameService", function () {

      var baseUrl = "http://localhost:8080/civilization/game/";
      this.setBaseUrl = function (url) {
        baseUrl = url;
      };

      this.$get = function ($http, $q, $log) {
        var games = [];
        $log.info("Creating game data service");

        var createGame = function (game) {
          games = [];
          return $http.post(baseUrl, game)
            .then(function (response) {
              return response.data;
            });
        };

        var joinGame = function (game) {
          games = [];
          return $http.put(baseUrl + game.id + "/join")
            .then(function (response) {
              return response.data;
            });
        };

        var getGameById = function (id) {
          var url = baseUrl + id;
          return $http.get(url)
            .then(function (response) {
              return response.data;
            });
        };

        var getAllGames = function () {
          if (games.length > 0) {
            $log.info("Got games from cache");
            return $q.when(games);
          } else {
            return $http.get(baseUrl)
              .then(function (response) {
                $log.info("Got games from API");
                games = response.data;
                return games;
              });
          }
        };

        var getAvailableTechs = function(gameid) {
          var url = baseUrl + gameid + "/techs";
          return $http.get(url)
            .then(function(response) {
              $log.info("Got all available techs");
              return response.data;
            });
        };

        return {
          getAllGames: getAllGames,
          joinGame: joinGame,
          getGameById: getGameById,
          createGame: createGame,
          getAvailableTechs: getAvailableTechs
        };
      };

    });
  });

}(angular.module("civApp")));
