'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function ($http, $q, $log, growl, currentUser, BASE_URL) {
    var baseUrl = BASE_URL + "/player/";
    this.setBaseUrl = function (url) {
      baseUrl = url;
    };

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
      var url = baseUrl + gameId + "/revealItem/";

      var sheetName = angular.lowercase(Object.keys(item)[0]);
      var itemDTO = {
        "name": nextElement(item).name,
        "ownerId": nextElement(item).ownerId,
        "sheetName": sheetName,
        "pbfId": gameId
      };

      $log.info("Before calling delete, json is ", angular.toJson(itemDTO));
      var configuration = {
        headers: {
          "Content-Type": "application/json"
        }
      };

      return $http.put(url, itemDTO, configuration)
        .success(function (response) {
          growl.success("Item revealed");
          return response;
        })
        .error(function (data) {
          growl.error("Item could not be revealed");
          return data;
        });
    };

    var discardItem = function (gameId, item) {
      var url = baseUrl + gameId + "/item";

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
          //TODO need to call get game again so that everything is refreshed
          growl.success("Item discarded");
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
