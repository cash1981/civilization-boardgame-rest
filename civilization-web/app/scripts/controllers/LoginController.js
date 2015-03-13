'use strict';
(function (module) {

  var LoginController = function (basicauth, currentUser, growl, loginRedirect, $modal, $log) {
    var model = this;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.registerUsername = null;
    model.registerEmail = null;
    model.registerPassword = null;
    model.registerVerification = null;

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
        //To inject stuff in the modal you need to add resolve
        /*resolve: {
          register : function() {
            return register;
          }
        }*/
      });

      modalInstance.result.then(function(register) {
        if(register) {
          $log.info(register.username);
          $log.info(register.email);
          $log.info(register.password);
          basicauth.register(register);
        }
      }, function () {
        //Cancel callback here
      });
    };

  };

  module.controller("LoginController", ['basicauth', 'currentUser', 'growl', 'loginRedirect', '$modal', '$log', LoginController]);

}(angular.module("civApp")));
