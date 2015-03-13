'use strict';
angular.module('civApp').controller('RegisterController', function ($scope, $modalInstance, $log, growl) {
  var model = this;
  $log.info("Reg instansiert");

  $scope.registerOk = function() {
    if(model.password !== model.verification) {
      growl.error("Passwords did not match");
      return;
    }

    var register = {
      'username' : model.registerUsername,
      'email' : model.registerEmail,
      'password' : $scope.password
    };

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
