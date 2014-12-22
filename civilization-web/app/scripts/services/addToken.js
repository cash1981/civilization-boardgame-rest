(function(module) {

    var addToken = function(currentUser, $q, $log) {

        return {
            request: function(config) {
                if(currentUser.profile.authorization_encoded) {
                    $log.info("Adding authorization to header " + "Bearer " + currentUser.profile.authorization_encoded);
                    config.headers.Authorization = "Bearer " + currentUser.profile.authorization_encoded;
                    //TODO I possibly need to add it in config.headers.common.Authorization
                }
                return $q.when(config);
            }
        };
    };

    module.factory("addToken", addToken);
    module.config(function($httpProvider) {
        $httpProvider.interceptors.push("addToken");
    });

})(angular.module("civApp"));
