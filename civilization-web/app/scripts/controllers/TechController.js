'use strict';
(function (module) {
  var TechController = function ($log, $routeParams, GameService, currentUser, Util, $scope, PlayerService) {
    var model = this;

    model.nextElement = function(obj) {
      return Util.nextElement(obj);
    };

    $scope.$watch(function () {
      return GameService.getGameById($routeParams.id);
    }, function (newVal) {
      if (!newVal) {
        return;
      }
      var game = newVal;
      model.techsChosen = game.player.techsChosen;
      putTechsInScope(model.techsChosen);
      return game;
    });

    function putTechsInScope(techs) {
      model.chosenTechs1 = [];
      model.chosenTechs2 = [];
      model.chosenTechs3 = [];
      model.chosenTechs4 = [];
      model.chosenTechs5 = [];
      model.availableTech1 = [];
      model.availableTech2 = [];
      model.availableTech3 = [];
      model.availableTech4 = [];

      //TODO refactor to lodash _forEach
      techs.forEach(function (tech) {
        var chosenTech = tech.tech || tech;
        if(!chosenTech) {
          return;
        }

        if(chosenTech.level === 1) {
          model.chosenTechs1.push(chosenTech);
        } else if(chosenTech.level === 2) {
          model.chosenTechs2.push(chosenTech);
        } else if(chosenTech.level === 3) {
          model.chosenTechs3.push(chosenTech);
        } else if(chosenTech.level === 4) {
          model.chosenTechs4.push(chosenTech);
        } else if(chosenTech.level === 5) {
          model.chosenTechs5.push(chosenTech);
        }
      });

      //Find out how many techs are available for each level
      for(var i = 0; i < (5-model.chosenTechs1.length); i++) {
        model.availableTech1.push(i);
      }
      for(var j = 0; j < (4-model.chosenTechs2.length); j++) {
        model.availableTech2.push(j);
      }
      for(var k = 0; k < (3-model.chosenTechs3.length); k++) {
        model.availableTech3.push(k);
      }
      for(var l = 0; l < (2-model.chosenTechs4.length); l++) {
        model.availableTech4.push(l);
      }
    }

    model.selectTech = function() {
      if($scope.selectedTech) {
        PlayerService.selectTech($routeParams.id, $scope.selectedTech)
          .then(function() {
            GameService.getAvailableTechs($routeParams.id)
              .then(function(techs) {
                model.allAvailableTechs = techs;
              });
          });
      }
    };

    model.removeTech = function(techname) {
      $log.info("Removing tech " + techname);
      PlayerService.removeTech($routeParams.id, techname)
        .then(function() {
          GameService.getAvailableTechs($routeParams.id)
            .then(function(techs) {
              model.allAvailableTechs = techs;
            });
        });
    };

    model.canRevealTech = function(log) {
        return $scope.userHasAccess && log && log.draw && log.draw.hidden && log.log.indexOf("researched") > -1;
    };

    model.revealTechFromLog = function(logid) {
      PlayerService.revealTech($routeParams.id, logid);
    };

    /* jshint ignore:start */
    var getAvailableTechs = GameService.getAvailableTechs($routeParams.id)
      .then(function(techs) {
        model.allAvailableTechs = techs;
      });

    var getChosenTechs = PlayerService.getChosenTechs($routeParams.id)
      .then(function(techs) {
        model.chosenTechs = techs;
        putTechsInScope(model.chosenTechs);
      });
    /* jshint ignore:end */

    var initialize = function() {
      model.allAvailableTechs = [];
      model.techsChosen = [];

      /* jshint ignore:start */
      getAvailableTechs;
      getChosenTechs;
      /* jshint ignore:end */
    };

    initialize();
  };

  module.controller("TechController",
    ["$log", "$routeParams", "GameService", "currentUser", "Util", "$scope", "PlayerService", TechController]);

}(angular.module("civApp")));
