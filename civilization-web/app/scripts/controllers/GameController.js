civApp.controller('GameController', function($scope, $log) {
    $http({
        method: 'GET',
        url: 'http://localhost:8080/civilization/game'
    }).success(function (result) {
        $log.info("Henter ut spill");
        $scope.games = result;
    });
});