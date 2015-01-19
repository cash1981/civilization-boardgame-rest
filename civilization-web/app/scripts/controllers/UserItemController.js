'use strict';
(function (module) {
  var UserItemController = function ($log, $routeParams, gameData, currentUser, $filter, ngTableParams, $scope, PlayerService) {
    var model = this;
    model.user = currentUser.profile;
    $scope.privateLogCollapse = false;
    $scope.itemCollapse = true;
    $scope.gpCollapse = true;
    $scope.unitCollapse = true;
    $scope.cultureCardsCollapse = true;
    $scope.civCollapse = true;
    $scope.hutsCollapse = true;
    $scope.villagesCollapse = true;

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

    model.itemName = function(obj) {
      var val = Object.keys(obj);
      if(val && val.length > -1) {
        return val[0];
      }
      return obj;
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
      //TODO hvordan kaller jeg pÃ¥ getGameById igjen?
      //dette funker ikke: updateGame($routeParams.id);
    };

    var gamePromise = gameData.getGameById($routeParams.id)
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


  };

  module.controller("UserItemController",
    ["$log", "$routeParams", "gameData", "currentUser", "$filter", "ngTableParams", "$scope", "PlayerService", UserItemController]);

}(angular.module("civApp")));
