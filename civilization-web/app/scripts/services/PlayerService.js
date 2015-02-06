'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function ($http, $q, $log, growl, currentUser) {
    var baseUrl = "http://localhost:8080/civilization/player/";
    this.setBaseUrl = function (url) {
      baseUrl = url;
    };

    var revealItem = function (gameId, logid) {
      var url = baseUrl + gameId + "/revealItem/" + logid;
      return $http.put(url)
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
        "hidden": nextElement(item).hidden,
        "used": nextElement(item).used,
        "description": nextElement(item).description,
        "type": nextElement(item).type,
        "sheetName": sheetName,
        "pbfId": gameId
      };

      $log.info("Before calling delete, json is ", angular.toJson(itemDTO));

      var configuration = {
        headers: {
          "Content-Type": "application/json"
        }
      };

      /* $http({
       url: url,
       dataType: "json",
       method: "DELETE",
       headers: {
       "Content-Type": "application/json"
       }
       }).success(function (response) {
       //TODO need to call get game again so that everything is refreshed
       growl.success("Item discarded");
       return response;
       }).error(function (data) {
       growl.error("Item could not be discarded");
       return data;
       });
       */
      $http.delete(url, itemDTO, configuration)
        .success(function (response) {
          //TODO need to call get game again so that everything is refreshed
          growl.success("Item discarded");
          return response;
        }).error(function (data) {
          growl.error("Item could not be discarded");
          return data;
        });

      //TODO Flytt denne i util? Duplikat i UserItemController
      function nextElement(obj) {
        if (obj) {
          var keys = Object.keys(obj);
          if (keys && keys.length > 0) {
            return obj[keys[0]];
          }
        }
        return obj;
      }
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
          return data;
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
