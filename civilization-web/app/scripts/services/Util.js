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

    return {
      nextElement: nextElement
    };
  };

  module.factory("Util", util);

}(angular.module("civApp")));
