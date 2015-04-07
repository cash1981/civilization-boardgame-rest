'use strict';

(function () {
  var application = angular.module('civApp', [
    'ngAnimate',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngMessages',
    'ui.bootstrap',
    'ngTouch',
    'ab-base64',
    'angular-growl',
    'ngTable',
    'nya.bootstrap.select'
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
        templateUrl: 'views/game.html',
        controller: "ChatController as chatCtrl",
        resolve: {
          chatList: ["GameService", "$route", function(m, r) {
            //return m.getChatList(r.$$url.split('/')[2]);
            return m.getChatList(r.current.params.id);
          }]
        }
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
      .when('/logout', {
        redirectTo: '/'
      })
      .when('/endgame', {
        redirectTo: '/'
      })
      .otherwise({
        templateUrl: '404.html'
      });
  });

  application.config(function (growlProvider) {
    growlProvider.globalTimeToLive(7000);
    growlProvider.globalDisableCountDown(false);
    growlProvider.globalPosition('top-center');
    growlProvider.onlyUniqueMessages(true);
  })
    .constant('BASE_URL', 'http://localhost:8080/api');

}());
