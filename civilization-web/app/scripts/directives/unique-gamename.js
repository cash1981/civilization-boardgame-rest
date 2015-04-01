'use strict';
/**
 * Copied and modified from ng-signup-form
 * https://github.com/zemirco/ng-signup-form
 */
angular.module('civApp').directive('uniqueGamename', ['$http', 'BASE_URL', function($http, BASE_URL) {
  return {
    require: 'ngModel',
    link: function(scope, elem, attrs, ctrl) {
      scope.busy = false;
      scope.$watch(attrs.ngModel, function(value) {

        // hide old error messages
        ctrl.$setValidity('isTaken', true);
        ctrl.$setValidity('invalidChars', true);
        if(!value) {
          return;
        }

        var url = BASE_URL + '/game/check/gamename';
        scope.busy = true;
        $http.post(url, {name: value})
          .success(function() {
            // everything is fine -> do nothing
            scope.busy = false;
          })
          .error(function(data) {
            // display new error message
            if (data.isTaken) {
              ctrl.$setValidity('isTaken', false);
            } else if (data.invalidChars) {
              ctrl.$setValidity('invalidChars', false);
            }
            scope.busy = false;
          });
      });
    }
  };
}]);
