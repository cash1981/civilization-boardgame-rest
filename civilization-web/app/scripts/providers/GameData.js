(function (civApp) {

  civApp.config(function ($provide) {
    $provide.provider("gameData", function () {

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

        var revealItem = function (gameId, logid) {
          baseUrl = "http://localhost:8080/civilization/player/";
          var url = baseUrl + gameId + "/revealItem/" + logid;
          return $http.put(url)
            .success(function (response) {
              baseUrl = "http://localhost:8080/civilization/game/";
              return response;
            })
            .error(function(data) {
              baseUrl = "http://localhost:8080/civilization/game/";
              //TODO Sette error et sted
              return data;
            });
        };


        return {
          getAllGames: getAllGames,
          joinGame: joinGame,
          getGameById: getGameById,
          createGame: createGame,
          revealItem: revealItem
        };
      };

    });
  });

}(angular.module("civApp")));
