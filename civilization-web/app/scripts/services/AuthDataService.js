"use strict";
angular.module('civApp').factory('AuthDataService', [
  'localStorageService', '$base64', '$http', function (localStorageService, $base64, $http) {

    var current_auth_data = localStorageService.get('authorization_token');
    if (current_auth_data) {
      $http.defaults.headers.common['Authorization'] = "Basic " + current_auth_data;
    }
    return {
      setAuthData: function (authdata) {
        var encoded;
        if (!authdata) {
          return;
        }
        encoded = $base64.encode(authdata);
        localStorageService.set('authorization_token', encoded);
        return $http.defaults.headers.common['Authorization'] = "Basic " + encoded;
      },
      clearAuthData: function () {
        localStorageService.remove('authorization_token');
        return $http.defaults.headers.common['Authorization'] = '';
      },
      getAuthData: function () {
        return localStorageService.get('authorization_token');
      }
    };
  }
]);
