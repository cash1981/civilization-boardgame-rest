'use strict';
(function (module) {
var GameController = function ($log, $routeParams, GameService, PlayerService, currentUser, $filter, ngTableParams, $scope, growl, $modalN) {
  var model = this;
  model.user = currentUser.profile;
  $scope.userHasAccess = false;
  model.yourTurn = false;
  var gameId = $routeParams.id;

  $scope.$watch(function () {
    return GameService.getGameById(gameId);
  }, function (newVal) {
    if (!newVal) {
      return;
    }
    var game = newVal;
    model.game = game;
    console.log(game.name);
    $scope.userHasAccess = game.player && game.player.username === model.user.username;
    model.yourTurn = game.player && game.player.yourTurn;

    if(model.yourTurn) {
      growl.success("<strong>It's your turn!</strong>");
    }
    console.log("Kaller reload");
    model.tableParams.reload();
    return game;
  });

  model.endTurn = function () {
    $log.info("Ending turn");
    PlayerService.endTurn(gameId);
  };

  //In scope so that we can use it from another view which is included
  $scope.canInitiateDraw = function(log) {
    return $scope.userHasAccess && log && log.draw && !log.draw.undo && log.log.indexOf("drew") > -1;
  };

  //In scope so that we can use it from another view which is included
  $scope.initiateUndo = function(logid) {
    GameService.undoDraw($routeParams.id, logid);
  };

  //In scope so that we can use it from another view which is included
  $scope.canVote = function(log) {
    var hasVoted = false;
    if($scope.userHasAccess && log && log.draw && log.draw.undo && log.log.indexOf("undo") > -1) {
      //Take out the users
      var votes = log.draw.undo.votes;

      for(var vote in votes) {
        if(vote == model.user.id) {
          return false;
        }
      }

      growl.warning("An undo was requested which needs your vote");
      return true;
    }
    return hasVoted;
  };

  $scope.openModalVote = function(size) {
    var modalInstance = $modal.open({
      templateUrl: 'modalVote.html',
      controller: 'VoteController',
      size: size,
      resolve: {
        itemToUndo: function () {
          return $scope.itemToUndo;
        }
      }
    });

    modalInstance.result.then(function(vote) {
      $log.info('Vote was ' + vote);
      //TODO call vote service
    }, function () {
      $log.info('Modal dismissed at: ' + new Date());
    });
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
      if (!model.game) {
        $defer.reject("No game yet");
        return;
      }
      var game = model.game;
      var orderedData = params.sorting() ? $filter('orderBy')(game.publicLogs, params.orderBy()) : game.publicLogs;
      params.total(game.publicLogs.length);
      $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
    },
    $scope: { $data: {}, $emit: function () {}}
  });

};

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", "PlayerService", "currentUser", "$filter", "ngTableParams", "$scope", "growl", "$modal", GameController]);

}(angular.module("civApp")));
