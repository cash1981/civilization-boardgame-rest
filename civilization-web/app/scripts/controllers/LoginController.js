'use strict';
(function (module) {

  var LoginController = function (basicauth, currentUser, growl, loginRedirect, $modal, $log) {
    var model = this;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.login = function (form) {
      if (form.$valid) {
        basicauth.login(model.username, model.password)
          .then(loginRedirect.redirectPreLogin);
        model.password = "";
      }
    };

    model.signOut = function () {
      basicauth.logout();
    };

    model.openSignup = function(size) {
      var modalInstance = $modal.open({
        templateUrl: 'signup.html',
        controller: 'RegisterController',
        size: size
      });

      modalInstance.result.then(function(user) {

      }, function () {
        $log.info('Modal dismissed at: ' + new Date());
      });
    };

  };

  module.controller("LoginController", ['basicauth', 'currentUser', 'growl', 'loginRedirect', '$modal', $log, LoginController]);

}(angular.module("civApp")));
