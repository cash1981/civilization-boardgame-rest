'use strict';
(function (module) {
  var GameController = function ($log, $routeParams, GameService, currentUser, $filter, ngTableParams) {
    var model = this;
    model.user = currentUser.profile;

    $log.info("Inside Game");
    var gamePromise = GameService.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
        return game;
      });

    model.getPublicLogs = function () {
      return model.game.publicLogs;
    };

    model.userHasAccess = function() {
      return gamePromise.then(function (game) {
        $log.info("Checking if user has access");
        return game.player.username == model.user.username;
      });
    };

    model.tableParams = new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        name: 'asc'     // initial sorting
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

  };

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", "currentUser", "$filter", "ngTableParams", GameController]);

}(angular.module("civApp")));
