'use strict';
(function (module) {
  var UserItemController = function ($log, $routeParams, GameService, currentUser, $filter, ngTableParams, $scope, PlayerService) {
    var model = this;
    model.user = currentUser.profile;
    $scope.privateLogCollapse = false;
    $scope.itemCollapse = false;
    $scope.gpCollapse = false;
    $scope.unitCollapse = false;
    $scope.cultureCardsCollapse = false;
    $scope.civCollapse = false;
    $scope.hutsCollapse = false;
    $scope.villagesCollapse = false;

    //Returns the next element in the array
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
        //TODO lodash doesn't work? return _.capitalize(key[0]);
        return toTitleCase(key[0]);
      }
      return item;
    };

    model.itemDetail = function(item) {
      var returnValue = null;
      $.each(item, function(index, element) {
        if(index === "aircraft" || index === "mounted" || index === "infantry" || index === "artillery") {
          var name = toTitleCase(index);
          var details = element.attack + "." + element.health;
          returnValue = name + " " + details;
        }
/* TODO FIXME for some reason when adding this code below, nothing is printed out
        else if(element.name) {
          var returnValue = element.name;
          if(element.type) {
            returnValue = returnValue + " Type: " + element.type;
          }
          if(element.description) {
            returnValue = returnValue + " Description: " + element.description;
          }
        }*/
        return returnValue;
      });

      return returnValue;
    };

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

    model.revealItem = function (gamelogid) {
      var response = PlayerService.revealItem($routeParams.id, gamelogid);
      $log.info("Revealed item, response is " + response);
      //TODO hvordan kaller jeg på getGameById?
      //dette funker ikke: updateGame($routeParams.id);
    };

    model.discardItem = function (item) {
      $log.info("Discard item " + item.name);
      var response = PlayerService.discardItem($routeParams.id, item);
      //TODO hvordan kaller jeg på getGameById?
      //dette funker ikke: updateGame($routeParams.id);
    };

    var gamePromise = GameService.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
        $scope.userHasAccess = game.player && game.player.username === model.user.username;
        model.yourTurn = game.player && game.player.yourTurn;

        model.items = game.player.items;
        model.techsChosen = game.player.techsChosen;
        model.civs = game.player.civs;
        model.cultureCards = game.player.cultureCards;
        model.greatPersons = game.player.greatPersons;
        model.huts = game.player.huts;
        model.villages = game.player.villages;
        model.tiles = game.player.tiles;
        //TODO model.units add infantries,aircrafts,artilleries,mounteds if not empty

        return game;
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
        gamePromise.then(function (game) {
          var orderedData = params.sorting() ? $filter('orderBy')(game.privateLogs, params.orderBy()) : game.privateLogs;
          params.total(game.privateLogs.length);
          $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    function toTitleCase(str) {
      return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
    }
  };

  module.controller("UserItemController",
    ["$log", "$routeParams", "GameService", "currentUser", "$filter", "ngTableParams", "$scope", "PlayerService", UserItemController]);

}(angular.module("civApp")));
