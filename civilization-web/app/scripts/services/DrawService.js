'use strict';
(function (civApp) {

  civApp.factory('DrawService', ["$http", "$q", "$log", "growl", "currentUser", "BASE_URL", "GameService", "Util", function ($http, $q, $log, growl, currentUser, BASE_URL, GameService, Util) {
    var baseUrl = BASE_URL + "/draw/";

    var drawUnitsFromHand = function(gameId, numOfUnits) {
      var url = baseUrl + gameId + "/battle";

      return $http({
        url: url,
        method: "PUT",
        params: {numOfUnits: numOfUnits}
      })
        .success(function (response) {
          if(response.length > 0) {
            growl.success("Units added to battlehand");
          } else {
            growl.warning("You have no units to draw");
          }
          return response;
        })
        .success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function (data) {
          $log.error(data);
          growl.error("Could not add units to battlehand for unknown reason");
          return data;
        });
    };

    var drawBarbarians = function (gameId) {
      var url = baseUrl + gameId + "/battle/barbarians";

      return $http.put(url)
        .success(function (response) {
          growl.success("Barbarians have been drawn");
          return response;
        })
        .success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function (data) {
          $log.error(data);
          if(data.status === 412) {
            growl.error("Cannot draw more barbarians until the others are discarded");
          } else {
            growl.error("Unable to draw barbarian units");
          }
          return data;
        });
    };

    var discardBarbarians = function(gameId) {
      var url = baseUrl + gameId + "/battle/discard/barbarians";

      return $http.post(url)
        .success(function (response) {
          growl.success("Barbarians discarded");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function () {
          growl.error("Could not discard barbarians for unknown reason");
          return $q.reject();
        });
    };

    var revealHand = function(gameId) {
      var url = baseUrl + gameId + "/battlehand/reveal";
      return $http.put(url)
        .success(function (response) {
          growl.success("Units are revealed and discarded from hand");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function () {
          growl.error("Units could not be revealed and discarded");
        });
    };

    var drawItem = function (gameId, sheetName) {
      var url = baseUrl + gameId + "/" + sheetName;
      return $http.post(url)
        .success(function (response) {
          growl.success("Item successfully drawn");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function (data, status) {
          if (status === 410) {
            growl.error("There are no more " + sheetName + " to draw!");
          } else {
            growl.error("Item could not be drawn");
          }
        });
    };

    var loot = function (gameId, sheetName, playerId) {
      var url = baseUrl + gameId + "/" + sheetName + "/loot/" + playerId;
      return $http.post(url)
        .success(function (response) {
          var item = Util.nextElement(response);
          growl.success("Item " + item.name + " looted by another player");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function(data) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
          growl.error("Item could not be lootet");
          return data;
        });
    };

    return {
      drawUnitsFromHand: drawUnitsFromHand,
      revealHand: revealHand,
      discardBarbarians: discardBarbarians,
      drawBarbarians: drawBarbarians,
      drawItem: drawItem,
      loot: loot
    };

  }]);

}(angular.module("civApp")));
