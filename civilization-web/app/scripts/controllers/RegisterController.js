'use strict';
angular.module('civApp').controller('RegisterController', function ($scope, $modalInstance, $log, growl) {
  var model = this;

  $scope.registerOk = function() {
    if(!model.verification && !model.password && model.password !== model.verification) {
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

  $scope.registerCancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
