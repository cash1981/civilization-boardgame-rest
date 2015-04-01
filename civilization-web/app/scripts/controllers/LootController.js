'use strict';
angular.module('civApp').controller('LootController', ["players", "sheetName", "currentUser", "$scope", "$modalInstance", function (players, sheetName, currentUser, $scope, $modalInstance) {
  var model = this;
  model.players = players;
  model.sheetName = sheetName;

  model.ok = function() {
    $modalInstance.close({
      playerId: model.playerLootChosen.playerId,
      sheetName: sheetName
    });
  };

  model.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
}]);
