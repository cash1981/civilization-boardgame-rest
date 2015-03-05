'use strict';
angular.module('civApp').controller('RegisterController', function ($scope, $modalInstance, $log) {
  var model = this;
  $log.info("Reg instansiert");
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
    $log.info(model.username);
    $log.info(model.email);
    $log.info(model.password);
    $log.info(model.verification);

    if(model.password == model.verification) {
      $log.info("Password matches");
    }

    $modalInstance.dismiss('cancel');
  };
});
