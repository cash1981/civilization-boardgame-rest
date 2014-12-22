angular.module('civApp').factory('RequestInterceptor',
    ['AuthDataService', '$log', function (AuthDataService, $log) {
        var interceptor = {
            request: function (config) {
                $log.info("Inside RequestInterceptor checking auth data");
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
