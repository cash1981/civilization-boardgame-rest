(function (module) {

  var LoginController = function (basicauth, currentUser, alerting, loginRedirect) {
    var model = this;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.login = function (form) {
      if (form.$valid) {
        basicauth.login(model.username, model.password)
          .then(loginRedirect.redirectPreLogin)
          .catch(alerting.errorHandler("Could not login"));
        model.password = "";
      }
    };

    model.signOut = function () {
      basicauth.logout();
    };
  };

  module.controller("LoginController", ['basicauth', 'currentUser', 'alerting', 'loginRedirect', LoginController]);

}(angular.module("civApp")));
