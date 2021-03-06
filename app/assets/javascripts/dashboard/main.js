/**
 * Dashboard shown after user is logged in.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.dashboard',
      module;

  module = angular.module(name, []);

  module.controller('DashboardCtrl', require('./DashboardCtrl'));
  module.config(require('./states'));

  return {
    name: name,
    module: module
  };
});
