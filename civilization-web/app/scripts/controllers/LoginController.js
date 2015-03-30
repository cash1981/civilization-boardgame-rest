'use strict';
(function (module) {

  var LoginController = function (basicauth, currentUser, growl, loginRedirect, Option, $modal, $log) {
    var model = this;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.registerUsername = null;
    model.registerEmail = null;
    model.registerPassword = null;
    model.registerVerification = null;
    model.showOption = Option.value;

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
        controller: 'RegisterController as registerCtrl',
        size: size
      });

      modalInstance.result.then(function(register) {
        if(register) {
          basicauth.register(register);
        }
      }, function () {
        //Cancel callback here
      });
    };

  };

  module.controller("LoginController", ['basicauth', 'currentUser', 'growl', 'loginRedirect', 'Option', '$modal', '$log', LoginController]);

}(angular.module("civApp")));
