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
    'ab-base64',
    'ngTable'
  ])
  .config(function ($routeProvider, $httpProvider) {
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

    // Some config for IE stuff and just in case
    /*$httpProvider.defaults.useXDomain = true;
    $httpProvider.defaults.withCredentials = true;
    var contentTypeHeader = "Content-Type";
    var jsonMediaType = "application/json";
    $httpProvider.defaults.headers.post[contentTypeHeader] = jsonMediaType;
    $httpProvider.defaults.headers.put[contentTypeHeader] = jsonMediaType;
*/
    //TODO Also added interceptor in RequestInterceptor.js, move it here
  });
