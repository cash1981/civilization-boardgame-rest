'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function ($http, $q, $log, growl, currentUser, BASE_URL, GameService) {
    var baseUrl = BASE_URL + "/player/";

    /**
     * Returns next element in the object
     */
    function nextElement(obj) {
      if (obj) {
        var keys = Object.keys(obj);
        if (keys && keys.length > 0) {
          return obj[keys[0]];
        }
      }
      return obj;
    }

    var revealItem = function (gameId, item) {
      var url = baseUrl + gameId + "/item/reveal";

      var sheetName = angular.lowercase(Object.keys(item)[0]);
      var itemDTO = {
        "name": nextElement(item).name,
        "ownerId": nextElement(item).ownerId,
        "sheetName": sheetName,
        "pbfId": gameId
      };

      $log.info("Before calling put, json is ", angular.toJson(itemDTO));
      var configuration = {
        headers: {
          "Content-Type": "application/json"
        }
      };

      return $http.put(url, itemDTO, configuration)
        .success(function (response) {
          growl.success("Item revealed");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function () {
          growl.error("Item could not be revealed");
        });
    };

    var discardItem = function (gameId, item) {
      var url = baseUrl + gameId + "/item/discard";

      var sheetName = angular.lowercase(Object.keys(item)[0]);
      var itemDTO = {
        "name": nextElement(item).name,
        "ownerId": nextElement(item).ownerId,
        "sheetName": sheetName,
        "pbfId": gameId
      };

      $log.info("Before calling post, json is ", angular.toJson(itemDTO));

      var configuration = {
        headers: {
          "Content-Type": "application/json"
        }
      };

      $http.post(url, itemDTO, configuration)
        .success(function (response) {
          growl.success("Item discarded");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        }).error(function (data) {
          growl.error("Item could not be discarded");
          return data;
        });
    };

    var endTurn = function (gameId) {
      var url = baseUrl + gameId + "/endturn";
      return $http.put(url)
        .success(function (response) {
          growl.success("Turn ended");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function (data) {
          growl.error("Could not end turn");
          return data;
        });
    };

    var getChosenTechs = function (gameId) {
      var url = baseUrl + gameId + "/tech/" + currentUser.profile.id;
      return $http.get(url)
        .then(function (response) {
          return response;
        }, function (data) {
          $log.error(data);
          growl.error("Could not get chosen techs");
          $q.reject();
        });
    };

    return {
      revealItem: revealItem,
      discardItem: discardItem,
      endTurn: endTurn,
      getChosenTechs: getChosenTechs
    };

  });

}(angular.module("civApp")));
