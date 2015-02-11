'use strict';
angular.module('civApp').controller('VoteController', function ($scope, $modalInstance) {

  $scope.voteOk = function () {
    $modalInstance.close(true);
  };

  $scope.voteNok = function () {
    $modalInstance.close(false);
  };

  $scope.voteCancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
