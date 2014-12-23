'use strict';
(function (module) {

  var basicauth = function () {

    //var url = "/login";
    var url = "http://localhost:8080/civilization/login";

    this.setUrl = function (newUrl) {
      url = newUrl;
    };

    this.$get = function ($http, formEncode, currentUser, base64, $log) {

      var processToken = function (username, password) {
        return function (response) {
          $log.info("Is player info here " + response)
          $log.info("Is player info here response.data" + response.data)
          currentUser.profile.username = username;
          var encoded = base64.encode(username + ":" + password);
          $log.info("Encoded string from username:password= " + username + ":" + password + " is encoded= " + encoded);
          currentUser.profile.authorizationEncoded = encoded;
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

        return $http.post(url, data, configuration).then(processToken(username, password));
      };

      var logout = function () {
        currentUser.profile.username = "";
        currentUser.profile.authorizationEncoded = "";
        currentUser.remove();
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
