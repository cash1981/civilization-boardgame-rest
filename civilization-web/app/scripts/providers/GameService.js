'use strict';
(function (civApp) {

  civApp.config(["$provide", function ($provide) {
    $provide.provider("GameService", ["BASE_URL", function (BASE_URL) {
      var games = {};
      var playersCache = {};
      var loading = {};
      var playerLoading = {};
      var baseUrl = BASE_URL + "/game/";

      this.$get = ["$http", "$log", "growl", "$location", "$q", "formEncode", function ($http, $log, growl, $location, $q, formEncode) {
        var createGame = function (game) {

          var newGameDTO = {
            "name": game.name,
            "type": game.type,
            "numOfPlayers": game.numOfPlayers,
            "color": game.color
          };

          //$log.info("Before calling post, json is ", angularN.toJson(newGameDTO));

          return $http.post(baseUrl, newGameDTO)
            .success(function (data, status, headers) {
              growl.success("Game created!");
              var loc = headers('Location');
              if(loc) {
                /* jshint ignore:start */
                var gameid = _.last(loc.split('/'));
                if(gameid) {
                  $location.path('/game/' + gameid);
                }
                /* jshint ignore:end */
              }
              return data;
            })
            .error(function (data) {
              growl.error("Could not create game");
              return data;
            });
        };

        var joinGame = function (game) {
          return $http.post(baseUrl + game.id + "/join")
            .then(function (response) {
              return response.data;
            });
        };

        var fetchGameByIdFromServer = function (id) {
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

          fetchGameByIdFromServer(id);
        };

        var getAllGames = function () {
          return $http.get(baseUrl, {cache: true})
            .then(function (response) {
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
              fetchGameByIdFromServer(gameid);
              return response;
            })
            .error(function (data, status) {
              if(status === 400) {
                growl.error("Undo already initiated");
              } else {
                growl.error("Could not initiate undo for unknown reason");
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

        var voteYes = function(gameid, logid) {
          $log.info("Voted no on gameid " + gameid + " and logid " + logid);
          var url = baseUrl + gameid + "/vote/" + logid + "/yes";
          $http.put(url)
            .success(function (response) {
              growl.success("You voted yes!");
              return response;
            }).success(function (response) {
              fetchGameByIdFromServer(gameid);
              return response;
            })
            .error(function (data, status) {
              if(status === 412) {
                growl.error("Could not register vote. Nothing to vote on");
              } else {
                growl.error("Could not vote for unknown reason");
              }
              return data;
            });
        };

        var voteNo = function(gameid, logid) {
          $log.info("Voted no on gameid " + gameid + " and logid " + logid);
          var url = baseUrl + gameid + "/vote/" + logid + "/no";
          $http.put(url)
            .success(function (response) {
              growl.success("You voted no!");
              return response;
            }).success(function (response) {
              fetchGameByIdFromServer(gameid);
              return response;
            })
            .error(function (data, status) {
              if(status === 412) {
                growl.error("Could not register vote. Nothing to vote on");
              } else {
                growl.error("Could not vote for unknown reason");
              }
              return data;
            });
        };

        var getChatList = function (gameid) {
          var url = baseUrl + gameid + "/chat/";
          return $http.get(url, {cache: true})
            .then(function (response) {
              return response.data;
            });
        };

        var chat = function (gameid, message) {
          if(!gameid || !message) {
            return $q.reject('No chat message');
          }

          var url = baseUrl + gameid + "/chat/";

          var configuration = {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          };

          var data = formEncode({
            message: encodeURIComponent(message)
          });

          return $http.post(url, data, configuration)
            .then(function(response) {
              return response.data;
            });
        };

        var players = function(gameid) {
          if (playerLoading[gameid]) {
            return;
          }

          if(playersCache[gameid]) {
            return playersCache[gameid];
          }

          return fetchPlayersFromServer(gameid);
        };

        var fetchPlayersFromServer = function (gameid) {
          if(!gameid) {
            return $q.reject("No gameid");
          }
          var url = baseUrl + gameid + "/players";
          playerLoading[gameid] = true;
          return $http.get(url, {cache: true})
            .then(function (response) {
              playersCache[gameid] = response.data;
              playerLoading[gameid] = false;
              return response.data;
            });
        };

        var endGame = function (gameid) {
          return $http.delete(baseUrl + gameid)
            .then(function (response) {
              growl.info("Game has ended");
              return response.data;
            });
        };

        var withdrawFromGame = function (gameid) {
          return $http.post(baseUrl + gameid + "/withdraw")
            .then(function (response) {
              growl.info("You have withdrawn from the game");
              return response.data;
            });
        };

        return {
          getAllGames: getAllGames,
          getGameById: getGameById,
          fetchGameByIdFromServer: fetchGameByIdFromServer,
          joinGame: joinGame,
          createGame: createGame,
          undoDraw: undoDraw,
          getAvailableTechs: getAvailableTechs,
          voteYes: voteYes,
          voteNo: voteNo,
          getChatList: getChatList,
          chat: chat,
          players: players,
          fetchPlayersFromServer: fetchPlayersFromServer,
          endGame: endGame,
          withdrawFromGame: withdrawFromGame
        };
      }];

    }]);
  }]);

}(angular.module("civApp")));
