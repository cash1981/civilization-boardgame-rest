'use strict';
(function (module) {
  var UserItemController = function ($log, $routeParams, GameService, currentUser, $filter, ngTableParams, $scope, PlayerService) {
    var model = this;

    model.user = currentUser.profile;
    model.allAvailableTechs = [];
    model.chosenTechs = [];
    model.chosenTechs1 = [];
    model.chosenTechs2 = [];
    model.chosenTechs3 = [];
    model.chosenTechs4 = [];
    model.chosenTechs5 = [];
    $scope.privateLogCollapse = false;
    $scope.itemCollapse = false;
    $scope.gpCollapse = false;
    $scope.unitCollapse = true;
    $scope.cultureCardsCollapse = false;
    $scope.civCollapse = false;
    $scope.hutsCollapse = false;
    $scope.villagesCollapse = false;
    model.yourTurn = false;
    model.items = [];
    model.techsChosen = [];
    model.civs = [];
    model.cultureCards = [];
    model.greatPersons = [];
    model.huts = [];
    model.villages = [];
    model.tiles = [];
    model.units = [];

    /**
     * Returns the next element in the object
     * @param obj
     * @returns obj.next
     */
    model.nextElement = function(obj) {
      if(obj) {
        var keys = Object.keys(obj);
        if(keys && keys.length > 0) {
          return obj[keys[0]];
        }
      }
      return obj;
    };

    model.itemName = function(item) {
      var key = Object.keys(item);
      if(key && key.length > -1) {
        return _.capitalize(key[0]);
      }
      return item;
    };

    model.revealItem = function (item) {
      var response = PlayerService.revealItem($routeParams.id, item);
      $log.info("Revealed item, response is " + response);
    };

    model.discardItem = function (item) {
      $log.info("Discard item " + item.name);
      PlayerService.discardItem($routeParams.id, item);
    };

    $scope.$watch(function () {
      return GameService.getGameById($routeParams.id);
    }, function (newVal) {
      if (!newVal) {
        return;
      }
      var game = newVal;
      model.game = game;
      model.techsChosen = game.player.techsChosen;
      model.cultureCards = [];
      model.greatPersons = [];
      model.huts = [];
      model.villages = [];
      model.tiles = [];
      model.units = [];
      model.items = [];
      readKeysFromItems(game.player.items);
      model.tablePrivateLog.reload();
      return game;
    });

    function readKeysFromItems(items) {
      items.forEach(function (item) {
        var itemKey = Object.keys(item)[0];
        if ("cultureI" == itemKey || "cultureII" == itemKey || "cultureIII" == itemKey) {
          model.cultureCards.push(item);
        } else if ("greatperson" == itemKey) {
          model.greatPersons.push(item);
        } else if ("hut" == itemKey) {
          model.huts.push(item);
        } else if ("village" == itemKey) {
          model.villages.push(item);
        } else if ("tile" == itemKey) {
          model.tiles.push(item);
        } else if ("aircraft" == itemKey || "mounted" == itemKey || "infantry" == itemKey || "artillery" == itemKey) {
          model.units.push(item);
        } else {
          model.items.push(item);
        }
      });
    }

    model.drawItem = function(itemToDraw) {
      if(itemToDraw) {
        PlayerService.drawItem($routeParams.id, itemToDraw)
      }
    };

    GameService.getAvailableTechs($routeParams.id)
      .then(function(techs) {
        model.allAvailableTechs = techs;
      });

    PlayerService.getChosenTechs($routeParams.id)
      .then(function(techs) {
        model.chosenTechs = techs;
        //TODO Sort by level, and add in corresponding var
        //if(level == 1) model.chosenTechs().put(techs)
      });

    model.tablePrivateLog = new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        created: 'desc'     // initial sorting
      }
    }, {
      total: 0, // length of data
      getData: function ($defer, params) {
        // use build-in angular filter
        // update table params
        if (!model.game) {
          $defer.reject("No game yet");
          return;
        }
        var game = model.game;
        var orderedData = params.sorting() ? $filter('orderBy')(game.privateLogs, params.orderBy()) : game.privateLogs;
        params.total(game.privateLogs.length);
        $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
      },
      $scope: { $data: {}, $emit: function () {}}
    });
  };

  module.controller("UserItemController",
    ["$log", "$routeParams", "GameService", "currentUser", "$filter", "ngTableParams", "$scope", "PlayerService", UserItemController]);

}(angular.module("civApp")));
