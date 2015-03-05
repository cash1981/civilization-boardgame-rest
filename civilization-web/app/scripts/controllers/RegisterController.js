'use strict';
angular.module('civApp').controller('RegisterController', function ($scope, $modalInstance, $log, register) {
  $log.info("Reg instansiert");

  $scope.registerOk = function() {
    $log.info(register.registerEmail);
    $log.info(register.registerUsername);
    $log.info(register.registerPassword);
    $log.info(register.registerVerification);
    $modalInstance.close(register);
  };

  /*$scope.voteOk = function () {
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
   */
  $scope.registerCancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
