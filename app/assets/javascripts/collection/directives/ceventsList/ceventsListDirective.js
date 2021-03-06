/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function ceventsListDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        participant: '=',
        collectionEventsPagedResult: '=',
        collectionEventTypes: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventsList/ceventsList.html',
      controller: CeventsListCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  //CeventsListCtrl.$inject = [];

  /**
   *
   */
  function CeventsListCtrl() {
    var vm = this;

    if (vm.collectionEventTypes.length <= 0) {
      throw new Error('no collection event types defined for this study');
    }
  }

  return ceventsListDirective;
});
