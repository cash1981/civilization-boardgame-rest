'use strict';

/**
 * @ngdoc overview
 * @name civApp
 * @description
 * # civApp
 *
 * Main module of the application.
 */
angular.module('civApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngMessages',
    'ngTouch'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html'
      })
      .when('/game/:id', {
        templateUrl: 'views/game.html'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
