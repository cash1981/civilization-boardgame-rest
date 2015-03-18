'use strict';
(function (civApp) {

  civApp.factory('PlayerService', function ($http, $q, $log, growl, currentUser, BASE_URL, GameService, Util) {
    var baseUrl = BASE_URL + "/player/";

    var revealItem = function (gameId, item) {
      var url = baseUrl + gameId + "/item/reveal";

      var itemDTO = {
        "name": Util.nextElement(item).name,
        "ownerId": Util.nextElement(item).ownerId,
        "sheetName": Util.nextElement(item).sheetName,
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

    var revealTech = function(gameid, logid) {
      var url = baseUrl + gameid + "/tech/reveal/" + logid;
      $http.put(url)
        .success(function (response) {
          growl.success("Research revealed!");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameid);
          return response;
        })
        .error(function (data) {
          growl.error("Could not reveal tech");
          return data;
        });
    };

    var discardItem = function (gameId, item) {
      var url = baseUrl + gameId + "/item/discard";

      var itemDTO = {
        "name": Util.nextElement(item).name,
        "ownerId": Util.nextElement(item).ownerId,
        "sheetName": Util.nextElement(item).sheetName,
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
      return $http.post(url)
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
          return response.data;
        }, function (data) {
          $log.error(data);
          growl.error("Could not get chosen techs");
          return $q.reject();
        });
    };

    var selectTech = function (gameId, selectedTech) {
      var url = baseUrl + gameId + "/tech/choose";

      return $http({
        url: url,
        method: "POST",
        params: {name: selectedTech.tech.name}
      })
      .success(function (response) {
        growl.success("Tech chosen successfully");
        return response;
      }).success(function (response) {
        GameService.fetchGameByIdFromServer(gameId);
        return response;
      }).error(function (data) {
          growl.error("Could not choose tech");
          return data;
        });
    };

    var removeTech  = function (gameId, techName) {
      var url = baseUrl + gameId + "/tech/remove";

      return $http({
        url: url,
        method: "DELETE",
        params: {name: techName}
      })
        .success(function (response) {
          growl.success("Tech removed successfully");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        }).error(function (data) {
          $log.error(data);
          growl.error("Could not remove tech");
          return data;
        });
    };

    return {
      revealItem: revealItem,
      revealTech: revealTech,
      discardItem: discardItem,
      endTurn: endTurn,
      selectTech: selectTech,
      getChosenTechs: getChosenTechs,
      removeTech: removeTech
    };

  });

}(angular.module("civApp")));
