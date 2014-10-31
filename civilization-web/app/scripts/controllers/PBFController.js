(function () {
    "use strict";

    var pbfController = function () {
        var allGames = function() {

        };

        return $http.get('localhost:8080/civilization/game').success(function (data) {
            json = data;
//                deferred.resolve(koder);
//                hentet = new Date();
//                koderBeingFetched = false;
        });
        return json;

        //lager json objekt med allGames navn og funksjonen allGames
        return {allGames:allGames};
    };

    var controllers = angular.module('controllers');
    controllers.controller('PBFController', []);



}());