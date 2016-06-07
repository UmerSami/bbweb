/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  templateMixinFactory.$inject = ['$templateCache'];

  function templateMixinFactory($templateCache) {

    var mixin = {
      putHtmlTemplates: putHtmlTemplates
    };

    return mixin;

    //---

    function putHtmlTemplates(/* template1, template2, ... */) {
      _.each(arguments, function (template) {
        $templateCache.put(template,
                           jasmine.getFixtures().getFixtureHtml_('../../../base/app' + template));
      });
    }
  }

  return templateMixinFactory;

});