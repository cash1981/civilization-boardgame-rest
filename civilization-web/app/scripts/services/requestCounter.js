'use strict';
(function (module) {

  var requestCounter = function ($q) {

    var requests = 0;

    var request = function (config) {
      requests += 1;
      return $q.when(config);
    };

    var requestError = function (error) {
      requests -= 1;
      return $q.reject(error);
    };

    var response = function (response) {
      requests -= 1;
      return $q.when(response);
    };

    var responseError = function (error) {
      requests -= 1;
      return $q.reject(error);
    };

    var getRequestCount = function () {
      return requests;
    };

    return {
      request: request,
      response: response,
      requestError: requestError,
      responseError: responseError,
      getRequestCount: getRequestCount
    };

  };
  requestCounter.$inject = ["$q"];

  module.factory("requestCounter", requestCounter);

  module.config(["$httpProvider", function ($httpProvider) {
    $httpProvider.interceptors.push("requestCounter");
  }]);

}(angular.module("civApp")));
