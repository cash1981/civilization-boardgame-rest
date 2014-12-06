'use strict';

/**
 * @ngdoc function
 * @name civApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the civApp
 */
angular.module('civApp')
        .controller('GameController', function($scope, $log, $http) {
            $http.get('http://localhost:8080/civilization/game')
                .success(function (result) {
                    $log.info("Henter ut spill fra main.js");
                    $scope.games = result;
                });

//            $http({
//                method: 'GET',
//                url: 'http://localhost:8080/civilization/game'
//            }).success(function (result) {
//                $log.info("Henter ut spill");
//                $scope.games = result;
//            });
        });
//  .controller('MainCtrl', function ($scope) {
//    $scope.awesomeThings = [
//      'HTML5 Boilerplate',
//      'AngularJS',
//      'Karma'
//    ];
//  });
