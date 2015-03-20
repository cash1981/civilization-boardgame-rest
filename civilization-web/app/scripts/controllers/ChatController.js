'use strict';
(function (module) {
  var ChatController = function ($log, currentUser, GameService, growl, $routeParams, $scope) {
    $log.info("Instansiating chat controller");
    var model = this;

    model.chatList = [];

    model.getChatList = function() {
      GameService.getChatList($routeParams.id)
        .then(function(data) {
          if(data) {
            model.chatList = data;
          }
        });
    };

    model.chat = function($event) {
      $log.info("chat");
      if(model.chatMessage) {
        $log.info(model.chatMessage);
        model.chatMessage = "";
        GameService.chat($routeParams.id, model.chatMessage)
          .then(function(data) {
            var newChat = data;
            if(newChat) {
              //model.chatList._push(newChat);
            }
          });

      } else {
        $event.preventDefault();
      }
    };

  };

  module.controller("ChatController",
    ["$log", "currentUser", "growl", "$routeParams", "$scope", ChatController]);

}(angular.module("civApp")));
