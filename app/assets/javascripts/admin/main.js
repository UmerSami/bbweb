/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin',
      module,
      centres = require('./centres/main'),
      studies = require('./studies/main'),
      users = require('./users/main'),
      directivesCentres = require('./directives/centres/main'),
      directivesStudies = require('./directives/studies/main'),
      services = require('./services/main');

  module = angular.module(name, [
    centres.name,
    studies.name,
    users.name,
    directivesCentres.name,
    directivesStudies.name,
    services.name,
    'biobank.common',
    'biobank.users',
    'biobank.studies'
  ]);

  module.config(require('./states'));
  module.directive('biobankAdmin', require('./directives/biobankAdmin/biobankAdminDirective'));
  module.directive('statusLine', require('./directives/statusLine/statusLineDirective'));

  return {
    name: name,
    module: module
  };
});
