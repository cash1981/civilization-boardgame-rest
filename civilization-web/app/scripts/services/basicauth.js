'use strict';
(function (module) {

  var basicauth = function () {
    this.$get = function ($http, formEncode, currentUser, base64, BASE_URL, growl, $rootScope) {
      var url = BASE_URL + "/login/";

      var processToken = function (username, password) {
        return function (response) {
          currentUser.profile.username = username;
          currentUser.profile.id = response.data.id;
          currentUser.profile.authorizationEncoded = base64.encode(username + ":" + password);
          currentUser.save();
          return username;
        }
      };

      var login = function (username, password) {

        var configuration = {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          }
        };

        var data = formEncode({
          username: username,
          password: password,
          grant_type: "password"
        });

        return $http.post(url, data, configuration)
          .then(
            processToken(username, password),
              function() {
                growl.error("Invalid login");
              }
        );
      };

      var logout = function () {
        currentUser.profile.username = "";
        currentUser.profile.password = "";
        currentUser.profile.authorizationEncoded = "";
        currentUser.profile.id = "";
        currentUser.remove();
        $rootScope.broadcast('logout');
      };

      return {
        login: login,
        logout: logout
      };
    }
  };

  module.config(function ($provide) {
    $provide.provider("basicauth", [basicauth]);
  });

}(angular.module("civApp")));
