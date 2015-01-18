'use strict';
(function (module) {

  var LoginController = function (basicauth, currentUser, growl, loginRedirect) {
    var model = this;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.login = function (form) {
      if (form.$valid) {
        basicauth.login(model.username, model.password)
          .then(loginRedirect.redirectPreLogin);
          //.catch(growl.error("Could not login"));
        model.password = "";
      }
    };

    model.signOut = function () {
      basicauth.logout();
    };
  };

  module.controller("LoginController", ['basicauth', 'currentUser', 'growl', 'loginRedirect', LoginController]);

}(angular.module("civApp")));
