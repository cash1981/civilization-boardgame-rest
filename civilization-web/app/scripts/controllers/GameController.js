'use strict';
(function (module) {
  var GameController = function ($log, $routeParams, gameData, currentUser, $filter, ngTableParams, $scope) {
    var model = this;
    model.user = currentUser.profile;
    $scope.userHasAccess = false;
    $scope.privateLogCollapse = true;
    $scope.itemCollapse = true;
    $scope.gpCollapse = true;
    $scope.unitCollapse = true;
    $scope.cultureCardsCollapse = true;
    $scope.civCollapse = true;
    $scope.hutsCollapse = true;
    $scope.villagesCollapse = true;
    model.yourTurn = false;

    var gamePromise = gameData.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
        $scope.userHasAccess = game.player && game.player.username === model.user.username;
        model.yourTurn = game.player && game.player.yourTurn;
        return game;
      });

    model.tableParams = new ngTableParams({
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
          var orderedData = params.sorting() ? $filter('orderBy')(game.publicLogs, params.orderBy()) : game.publicLogs;
          params.total(game.publicLogs.length);
          $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
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

  module.controller("GameController",
    ["$log", "$routeParams", "gameData", "currentUser", "$filter", "ngTableParams", "$scope", GameController]);

}(angular.module("civApp")));
