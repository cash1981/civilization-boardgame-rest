'use strict';
(function (civApp) {

  civApp.config(function ($provide) {
    $provide.provider("GameService", function () {

      var baseUrl = "http://localhost:8080/civilization/game/";
      this.setBaseUrl = function (url) {
        baseUrl = url;
      };

      this.$get = function ($http, $q, $log, growl) {
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

        var undoDraw = function(gameid, logid) {
          var url = baseUrl + gameid + "/undo/" + logid;
          $http.put(url)
            .success(function (response) {
            growl.success("Undo initiated!");
            return response;
            }).error(function (data, status) {
              if(status == 400) {
                growl.error("Undo already initiated")
              } else {
                growl.error("Could not initiate undo: " + data);
              }
              return data;
            });
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
          undoDraw: undoDraw,
          getAvailableTechs: getAvailableTechs
        };
      };

    });
  });

}(angular.module("civApp")));
