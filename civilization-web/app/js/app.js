'use strict';

// Declare app level module which depends on views, and components
var civApp = angular.module('myApp', ['ngRoute']);

civApp.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/view1'});
}]);
