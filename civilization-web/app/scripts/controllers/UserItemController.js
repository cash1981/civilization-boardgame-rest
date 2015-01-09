'use strict';
(function (module) {
  var UserItemController = function ($log, $routeParams, gameData, currentUser, $filter, ngTableParams, $scope) {
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

    model.yourTurn = false;

    model.revealItem = function (gamelogid) {
      return gameData.revealItem($routeParams.id, gamelogid)
    };

    var gamePromise = gameData.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
        $scope.userHasAccess = game.player && game.player.username === model.user.username;
        model.yourTurn = game.player && game.player.yourTurn;
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
    ["$log", "$routeParams", "gameData", "currentUser", "$filter", "ngTableParams", "$scope", UserItemController]);

}(angular.module("civApp")));
