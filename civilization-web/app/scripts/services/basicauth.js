'use strict';
(function (module) {

  var basicauth = function () {
    this.$get = function ($http, formEncode, currentUser, base64, BASE_URL, growl) {
      var url = BASE_URL + '/auth';

      var processToken = function (username, password) {
        return function (response) {
          currentUser.profile.username = username;
          currentUser.profile.id = response.data.id;
          currentUser.profile.authorizationEncoded = base64.encode(username + ':' + password);
          currentUser.save();
          return username;
        };
      };

      var login = function (username, password) {
        var configuration = {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          }
        };

        var data = formEncode({
          username: encodeURIComponent(username),
          password: encodeURIComponent(password),
          grant_type: 'password'
        });

        return $http.post(url + '/login', data, configuration)
          .then(
          processToken(username, password),
          function () {
            growl.error('Invalid login');
          }
        );
      };

      var logout = function () {
        currentUser.profile.username = '';
        currentUser.profile.password = '';
        currentUser.profile.authorizationEncoded = '';
        currentUser.profile.id = '';
        currentUser.remove();
      };

      var register = function (register) {
        var configuration = {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          }
        };

        var data = formEncode({
          username: encodeURIComponent(register.username),
          password: encodeURIComponent(base64.encode(register.password)),
          email: encodeURIComponent(register.email)
        });

        return $http.post(url + '/register', data, configuration)
          .success(function (response) {
            growl.success('User created');
            return response;
          }).success(function (response) {
            login(register.username, register.password);
            return response;
          }).error(function (data) {
            growl.error('Could not register');
            return data;
          });
      };

      return {
        login: login,
        logout: logout,
        register: register
      };
    };
  };

  module.config(["$provide", function ($provide) {
    $provide.provider('basicauth', [basicauth]);
  }]);

}(angular.module('civApp')));

