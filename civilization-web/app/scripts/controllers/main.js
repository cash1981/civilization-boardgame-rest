'use strict';

/**
 * @ngdoc function
 * @name civApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the civApp
 */
angular.module('civApp')
  .controller('MainCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
