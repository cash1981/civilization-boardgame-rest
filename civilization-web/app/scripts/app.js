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
    'ui.bootstrap',
    'ngTouch',
    'ngTable'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/list.html'
      })
      .when('/game/:id', {
        templateUrl: 'views/game.html'
        //Use resolve when you want the data to appear before going to the page
        /*
        ,resolve: {
             game = function(GameService) { GameService.getGame(gameId) }
             Then put this game in the controller, but you need to define controller here also
        }
         */
      })
      .otherwise({
        redirectTo: '/'
      });
  });
