/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('admin.services', ['biobank.common']);

  mod.factory('AdminService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      aggregateCounts : function() {
        return BbwebRestApi.call('GET', '/counts');
      }
    };
  }]);

});