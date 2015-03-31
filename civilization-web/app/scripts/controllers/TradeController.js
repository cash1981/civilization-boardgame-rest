'use strict';
angular.module('civApp').controller('TradeController', ["players", "item", "currentUser", "$scope", "$modalInstance", "$log", function (players, item, currentUser, $scope, $modalInstance, $log) {
  var model = this;
  $log.info("TradeController instansiated, and we have item " + item + "and players " + players);
  model.players = players;
  model.item = item;
  //model.playerTradeChosen = {};

  model.ok = function() {
    $log.info(item.name + " old owner " + item.ownerId);
    item.ownerId = model.playerTradeChosen.playerId;
    $log.info(item.name + " new owner " + item.ownerId);
    $modalInstance.close(item);
  };

  model.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
}]);
