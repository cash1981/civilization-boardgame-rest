'use strict';
(function (module) {
  var GameController = function ($log, $routeParams, GameService, PlayerService, currentUser, $filter, ngTableParams, $scope, growl) {
    var model = this;
    model.user = currentUser.profile;
    $scope.userHasAccess = false;
    model.yourTurn = false;
    var gameId = $routeParams.id;

    var gamePromise = GameService.getGameById(gameId)
      .then(function (game) {
        model.game = game;
        $scope.userHasAccess = game.player && game.player.username === model.user.username;
        model.yourTurn = game.player && game.player.yourTurn;

        if(model.yourTurn) {
          growl.success("<strong>It's your turn!</strong>");
        }

        return game;
      });

    model.endTurn = function () {
      $log.info("Ending turn");
      PlayerService.endTurn(gameId);
    };

    model.hasDraw = function(log) {
      return log && log.draw && !log.draw.undo && log.log.indexOf("drew") > -1;
    };

    model.initiateUndo = function(logid) {
      GameService.undoDraw($routeParams.id, logid);
    };

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

  };

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", "PlayerService", "currentUser", "$filter", "ngTableParams", "$scope", "growl", GameController]);

}(angular.module("civApp")));
