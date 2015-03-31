'use strict';
(function (module) {
var GameController = function ($log, $routeParams, GameService, PlayerService, currentUser, Util, GameOption, $filter, ngTableParams, $scope, growl, $modal) {
  var model = this;

  $scope.$watch(function () {
    return GameService.getGameById(model.gameId);
  }, function (newVal) {
    if (!newVal) {
      return;
    }
    var game = newVal;
    $scope.currentGame = game;
    var hasAccess = game.player && game.player.username === model.user.username && game.active;
    $scope.userHasAccess = hasAccess;
    GameOption.setShowValue(hasAccess);
    GameOption.setShowEndGameValue(game.player.gameCreator);
    model.yourTurn = game.player && game.player.yourTurn;

    if(model.yourTurn) {
      growl.info("<strong>It's your turn! Press end turn when you are done!</strong>");
    }

    //Check votes
    _.forEach(game.publicLogs, function(log) {
      if($scope.canVote(log)) {
        growl.warning("An undo was requested which needs your vote");
        return false;
      }
    });

    model.tableParams.reload();
    return game;
  });

  model.endTurn = function () {
    $log.info("Ending turn");
    PlayerService.endTurn(model.gameId);
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
        GameService.voteYes(model.gameId, vote.id);
      } else {
        GameService.voteNo(model.gameId, vote.id);
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
      if (!$scope.currentGame) {
        $defer.reject("No game yet");
        return;
      }
      var game = $scope.currentGame;
      var lastLog = _.last(game.publicLogs);
      if(lastLog && lastLog.log) {
        $scope.latestLog = lastLog.log;
      }

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
    return Util.nextElement(obj);
  };

  var initialize = function() {
    model.user = currentUser.profile;
    $scope.userHasAccess = false;
    model.yourTurn = false;
    model.gameId = $routeParams.id;
    model.GameOption = GameOption;
  };

  initialize();

};

  module.controller("GameController",
    ["$log", "$routeParams", "GameService", "PlayerService", "currentUser", "Util", 'GameOption', "$filter", "ngTableParams", "$scope", "growl", "$modal", GameController]);

}(angular.module("civApp")));
