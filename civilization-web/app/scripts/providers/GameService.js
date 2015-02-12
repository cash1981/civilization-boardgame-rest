'use strict';
(function (civApp) {

  civApp.config(function ($provide) {
    $provide.provider("GameService", function () {
      var games = {};
      var loading = {};
      var baseUrl = "http://localhost:8080/civilization/game/";
      this.setBaseUrl = function (url) {
        baseUrl = url;
      };

      this.$get = function ($http, $log, growl) {
        $log.info("Creating game data service");

        var createGame = function (game) {
          return $http.post(baseUrl, game)
            .then(function (response) {
              return response.data;
            });
        };

        var joinGame = function (game) {
          return $http.put(baseUrl + game.id + "/join")
            .then(function (response) {
              return response.data;
            });
        };

        var fetchGameById = function (id) {
          var url = baseUrl + id;
          loading[id] = true;
          return $http.get(url)
            .then(function (response) {
              games[id] = response.data;
              loading[id] = false;
              return response.data;
            });
        };

        var getGameById = function (id) {
          if (games[id]) {
            return games[id];
          }
          if (loading[id]) {
            return;
          }

          fetchGameById(id);
        };

        var getAllGames = function () {
          return $http.get(baseUrl, {cache: true})
            .then(function (response) {
              $log.debug("Got games");
              return response.data;
            });
        };

        var undoDraw = function(gameid, logid) {
          var url = baseUrl + gameid + "/undo/" + logid;
          $http.put(url)
            .success(function (response) {
            growl.success("Undo initiated!");
            return response;
            }).success(function (response) {
              fetchGameById(gameid);
              return response;
            })
            .error(function (data, status) {
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
