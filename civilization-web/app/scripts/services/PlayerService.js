'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function($http, $q, $log, growl) {
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
        .error(function(data) {
          growl.error("Item could not be revealed");
          return data;
        });
    };

    var discardItem = function(gameId, item) {
      var url = baseUrl + gameId + "/item";

      var nextElement;
      if(item) {
        var keys = Object.keys(item);
        if(keys && keys.length > 0) {
          nextElement = item[keys[0]];
        }
        else {
          nextElement = item;
        }
      }

      var sheetName = Object.keys(item)[0];
      //TODO to upper case this

      var itemDTO = {
        "name": item.nextElement.name,
        "ownerId": item.nextElement.ownerId,
        "hidden":item.nextElement.hidden,
        "used":item.nextElement.used,
        "description": item.nextElement.description,
        "type": item.nextElement.type,
        "sheetName": sheetName,
        "pbfId": gameId
      };

      $http.delete(url, itemDTO)
        .success(function (response) {
          //TODO need to call get game again so that everything is refreshed
          growl.success("Item discarded");
          return response;
      })
        .error(function(data) {
          growl.error("Item could not be discarded");
          return data;
        });
    };


    return {
      revealItem: revealItem,
      discardItem: discardItem
    };

  });

}(angular.module("civApp")));
