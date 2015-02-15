'use strict';
(function (module) {
var GameController = function ($log, $routeParams, GameService, PlayerService, currentUser, $filter, ngTableParams, $scope, growl, $modal) {
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
    $scope.userHasAccess = game.player && game.player.username === model.user.username;
    model.yourTurn = game.player && game.player.yourTurn;

    if(model.yourTurn) {
      growl.success("<strong>It's your turn! Press end turn when you are done!</strong>");
    }
    model.tableParams.reload();
    return game;
  });

  model.endTurn = function () {
    $log.info("Ending turn");
    PlayerService.endTurn(gameId);
  };

  //In scope so that we can use it from another view which is included
  $scope.canInitiateUndo = function(log) {
    return checkPermissionForVote(log) && !log.draw.undo;
  };

  function checkPermissionForVote(log) {
    return $scope.userHasAccess && log && log.draw && log.log.indexOf("drew") > -1;
  }

  //In scope so that we can use it from another view which is included
  $scope.initiateUndo = function(logid) {
    GameService.undoDraw($routeParams.id, logid);
  };

  //In scope so that we can use it from another view which is included
  $scope.canVote = function(log) {
    var hasVoted = false;
    if(checkPermissionForVote(log) && log.draw.undo) {
      //Take out the users
      var votes = log.draw.undo.votes;

      for(var vote in votes) {
        if(vote == model.user.id) {
          return false;
        }
      }
      //TODO This growl message is spamming us
      //growl.warning("An undo was requested which needs your vote");

      return true;
    }
    return hasVoted;
  };

  $scope.openModalVote = function(size, log) {
    var modalInstance = $modal.open({
      templateUrl: 'modalVote.html',
      controller: 'VoteController',
      size: size,
      resolve: {
        logToUndo: function () {
          return log;
        }
      }
    });

    modalInstance.result.then(function(vote) {
      $log.info('Vote was ' + vote.vote + ' and logid is ' + vote.id);
      if(vote.vote) {
        GameService.voteYes(gameId, vote.id);
      } else {
        GameService.voteNo(gameId, vote.id);
      }
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

};

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", "PlayerService", "currentUser", "$filter", "ngTableParams", "$scope", "growl", "$modal", GameController]);

}(angular.module("civApp")));
