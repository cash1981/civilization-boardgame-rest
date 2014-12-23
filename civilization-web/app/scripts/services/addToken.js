(function (module) {

  var addToken = function (currentUser, $q, $log) {

    return {
      request: function (config) {
        if (currentUser.profile.authorizationEncoded) {
          $log.info("Adding authorization to header " + "Basic " + currentUser.profile.authorizationEncoded);
          config.headers.Authorization = "Basic " + currentUser.profile.authorizationEncoded;
          //TODO I possibly need to add it in config.headers.common.Authorization
        }
        return $q.when(config);
      }
    };
  };

  module.factory("addToken", addToken);
  module.config(function ($httpProvider) {
    $httpProvider.interceptors.push("addToken");
  });

})(angular.module("civApp"));
