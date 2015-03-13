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

    var drawItem = function (gameId, sheetName) {
      var url = baseUrl + gameId + "/draw/" + sheetName;
      return $http.post(url)
        .success(function (response) {
          growl.success("Item successfully drawn");
          return response;
        }).success(function (response) {
          GameService.fetchGameByIdFromServer(gameId);
          return response;
        })
        .error(function (data, status) {
          if (status == 410) {
            growl.error("There are no more " + sheetName + " to draw!");
          } else {
            growl.error("Item could not be drawn");
          }
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
        method: "PUT",
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

    var drawUnitsForBattle = function(gameId, numOfUnits) {
      var url = baseUrl + gameId + "/battle/draw";

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
      var url = baseUrl + gameId + "/battle/draw/barbarians";

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
          if(data.status == 412) {
            growl.error("Cannot draw more barbarians until the others are discarded");
          } else {
            growl.error("Unable to draw barbarian units");
          }
          return data;
        });
    };

    var discardBarbarians = function(gameId) {
      var url = baseUrl + gameId + "/battle/discard/barbarians";

      return $http.delete(url)
        .then(function (response) {
          growl.success("Barbarians discarded");
          return response;
        }, function (data) {
          $log.error(data);
          growl.error("Could not discard barbarians for unknown reason");
          return $q.reject();
        })
    };

    return {
      revealItem: revealItem,
      revealTech: revealTech,
      drawItem: drawItem,
      discardItem: discardItem,
      endTurn: endTurn,
      selectTech: selectTech,
      getChosenTechs: getChosenTechs,
      removeTech: removeTech,
      drawUnitsForBattle: drawUnitsForBattle,
      drawBarbarians: drawBarbarians,
      discardBarbarians: discardBarbarians
    };

  });

}(angular.module("civApp")));
