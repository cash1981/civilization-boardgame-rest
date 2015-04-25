'use strict';
(function (module) {

  /**
   * Utility factory
   */
  var util = function() {

    /**
     * Returns the next element in the object
     * @param obj
     * @returns {*}
     */
    var nextElement = function(obj) {
      if(obj) {
        var keys = Object.keys(obj);
        if(keys && keys.length > 0) {
          return obj[keys[0]];
        }
      }
      return obj;
    };

    var mapLink = function(id) {
      var base = "https://docs.google.com/presentation/d/";
      var end = "/embed?start=false&loop=false&delayms=3000";
      return base + id + end;
    };

    var assetLink = function(id) {
      var base = "https://docs.google.com/spreadsheets/d/";
      var end = "/pubhtml?widget=true&amp;headers=false";
      return base + id + end;
    };

    return {
      nextElement: nextElement,
      mapLink: mapLink,
      assetLink: assetLink
    };
  };

  module.factory("Util", util);

}(angular.module("civApp")));
