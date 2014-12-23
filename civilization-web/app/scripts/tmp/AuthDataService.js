"use strict";
angular.module('civApp').factory('AuthDataService', [
  'localStorageService', 'ab-base64', '$http', '$log', function (localStorageService, base64, $http, $log) {
    $log.info("Inside AuthDataService");
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
        $log.info("Encoding data " + authdata);
        encoded = base64.encode(authdata);
        $log.info("Data encoded to: " + encoded);
        localStorageService.set('authorization_token', encoded);
        return $http.defaults.headers.common['Authorization'] = "Basic " + encoded;
      },
      clearAuthData: function () {
        localStorageService.remove('authorization_token');
        return $http.defaults.headers.common['Authorization'] = '';
      },
      getAuthData: function () {
        $log.info("Getting authorization_token from localStorage");
        return localStorageService.get('authorization_token');
      }
    };
  }
]);
