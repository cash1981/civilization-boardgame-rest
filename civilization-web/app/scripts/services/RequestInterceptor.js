angular.module('civApp').factory('RequestInterceptor',
  ['AuthDataService', function (AuthDataService) {
    var interceptor = {
      request: function (config) {
        var header = AuthDataService.getAuthData();
        if (header) {
          config.headers['Authorization'] = "Basic " + header;
        }
        return config;
      }
    };
    return interceptor;
  }
  ]);
angular.module('civApp').config([
  '$httpProvider', function ($httpProvider) {
    return $httpProvider.interceptors.push('RequestInterceptor');
  }
]);
