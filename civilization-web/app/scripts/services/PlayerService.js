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
          growl.success("Item could not be revealed");
          return data;
        });
    };

    return {
      revealItem: revealItem
    };

  });

}(angular.module("civApp")));
