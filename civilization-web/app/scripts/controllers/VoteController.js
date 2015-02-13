'use strict';
angular.module('civApp').controller('VoteController', function ($scope, $modalInstance) {

  $scope.voteOk = function (logid) {
    $scope.votedLogid = logid;
    $modalInstance.close(true);
  };

  $scope.voteNok = function (logid) {
    $scope.votedLogid = logid;
    $modalInstance.close(false);
  };

  $scope.voteCancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
