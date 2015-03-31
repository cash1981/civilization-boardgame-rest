'use strict';
angular.module('civApp').controller('VoteController', ["$scope", "$modalInstance", "$log", "logToUndo", function ($scope, $modalInstance, $log, logToUndo) {
  $scope.voteOk = function () {
    var vote = {
      id: logToUndo.id,
      vote: true
    };
    $modalInstance.close(vote);
  };

  $scope.voteNok = function () {
    var vote = {
      id: logToUndo.id,
      vote: false
    };
    $modalInstance.close(vote);
  };

  $scope.voteCancel = function () {
    $modalInstance.dismiss('cancel');
  };
}]);
