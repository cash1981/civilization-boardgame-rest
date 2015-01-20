'use strict';

(function () {

  var application = angular.module('civApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngMessages',
    'ui.bootstrap',
    'ngTouch',
    'ab-base64',
    'angular-growl',
    'ngTable'
  ]);

  application.config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/list.html',
        controller: "GameListController as gameListCtrl",
        resolve: {
          games: ["GameService", function(m) {
            return m.getAllGames();
          }]
        }
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
      .when('/help', {
        templateUrl: 'views/help.html'
      })
      .when('/about', {
        templateUrl: 'views/about.html'
      })
      .otherwise({
        redirectTo: '404.html'
      });
  });

  application.config(function (growlProvider) {
    growlProvider.globalTimeToLive(7000);
    growlProvider.globalDisableCountDown(false);
    growlProvider.globalPosition('top-center');
    growlProvider.onlyUniqueMessages(true);
  });

}());
