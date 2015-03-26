'use strict';
(function (module) {
  var ChatController = function (chatList, $log, currentUser, GameService, growl, $routeParams, $scope) {
    var model = this;

    model.chat = function($event) {
      $log.info("chat");
      if(model.chatMessage) {
        $log.info("message is " + model.chatMessage);
        GameService.chat($routeParams.id, model.chatMessage)
          .then(function(data) {
            var newChat = data;
            if(newChat) {
              newChat.message = decodeURIComponent(data.message);
              $scope.chatList.unshift(newChat);
              model.chatMessage = "";
            }
          });

      } else {
        $event.preventDefault();
      }
    };

    var init = function() {
      $scope.chatList = chatList;
      $log.info("Got chatlist");
    };

    init();

  };

  module.controller("ChatController",
    ["chatList", "$log", "currentUser", "GameService", "growl", "$routeParams", "$scope", ChatController]);

}(angular.module("civApp")));
