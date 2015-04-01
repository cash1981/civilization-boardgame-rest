'use strict';
(function (module) {

  var loginRedirect = function () {

    var loginUrl = "/auth";
    var lastPath = "";

    this.$get = ["$q", "$location", function ($q, $location) {

      return {

        responseError: function (response) {
          if (response.status === 401) {
            lastPath = $location.path();
            $location.path(loginUrl);
          }
          return $q.reject(response);
        },

        redirectPreLogin: function () {
          if (lastPath) {
            $location.path(lastPath);
            lastPath = "";
          } else {
            $location.path("/");
          }
        }
      };
    }];
  };

  module.provider("loginRedirect", loginRedirect);
  module.config(["$httpProvider", function ($httpProvider) {
    $httpProvider.interceptors.push("loginRedirect");
  }]);

}(angular.module("civApp")));
