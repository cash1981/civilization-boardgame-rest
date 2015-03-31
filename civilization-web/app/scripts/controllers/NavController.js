'use strict';
(function (module) {

  var NavController = function (GameService, $routeParams, basicauth, currentUser, growl, loginRedirect, GameOption, $modal, $log) {
    var model = this;
    model.GameOption = GameOption;
    model.user = currentUser.profile;

    model.username = "";
    model.password = "";
    model.user = currentUser.profile;

    model.registerUsername = null;
    model.registerEmail = null;
    model.registerPassword = null;
    model.registerVerification = null;

    model.clearOptions = function() {
      GameOption.setShowValue(false);
      GameOption.setShowEndGameValue(false);
    };

    model.endGame = function() {
      $log.info("Ending game");
      model.clearOptions();
    };

    model.withdrawGame = function() {
      $log.info("Withdraw game");
      model.clearOptions();
    };

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

  module.controller("NavController", ['GameService', '$routeParams', 'basicauth', 'currentUser', 'growl', 'loginRedirect', 'GameOption', '$modal', '$log', '$templateCache', NavController]);

}(angular.module("civApp")));
