'use strict';

/**
 * @ngdoc function
 * @name civApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the civApp
 */
angular.module('civApp')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
