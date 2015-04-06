'use strict';
(function (module) {
  var ChatController = function (chatList, $log, currentUser, GameService, growl, $routeParams, $scope) {
    var model = this;

    model.chat = function() {
      if(model.chatMessage) {
        GameService.chat($routeParams.id, model.chatMessage)
          .then(function (data) {
            var newChat = data;
            if (newChat) {
              newChat.message = data.message;
              $scope.chatList.unshift(newChat);
              model.chatMessage = "";
            }
          });
      }
    };

    var init = function() {
      $scope.chatList = chatList;
    };

    init();

  };

  module.controller("ChatController",
    ["chatList", "$log", "currentUser", "GameService", "growl", "$routeParams", "$scope", ChatController]);

}(angular.module("civApp")));
