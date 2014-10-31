(function () {
    'use strict';

    var PBFService = function($http) {

        var getGames = function() {
            return $http.get('localhost:8080/civilization/game');
        };
        return {getGames:getGames};
    };

    var services = angular.module('services');
    services.factory('PBFService', PBFService);
    PBFService.$inject = ['$http'];


}());
//parantesene betyr at denne kjøres (ready handler)