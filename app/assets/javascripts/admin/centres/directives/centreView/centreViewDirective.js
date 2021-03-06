/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function centreViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl : '/assets/javascripts/admin/centres/directives/centreView/centreView.html',
      controller: CentreViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreViewCtrl.$inject = [
    '$window',
    '$scope',
    '$state',
    '$timeout'
  ];

  function CentreViewCtrl($window, $scope, $state, $timeout) {
    var vm = this;

    vm.tabs = [
      { heading: 'Summary',   sref: 'home.admin.centres.centre.summary',   active: true },
      { heading: 'Studies',   sref: 'home.admin.centres.centre.studies',   active: true },
      { heading: 'Locations', sref: 'home.admin.centres.centre.locations', active: true },
    ];

    init();

    //--

    // initialize the panels to open state when viewing a new centre
    function init() {
      $scope.$on('centre-view', activeTabUpdate);

      if (vm.centre.id !== $window.localStorage.getItem('centre.panel.centreId')) {
        // this way when the user selects a new centre, the panels always default to open
        $window.localStorage.setItem('centre.panel.locations', true);

        // remember the last viewed centre
        $window.localStorage.setItem('centre.panel.centreId', vm.centre.id);
      }
    }

    /**
     * Updates the selected tab.
     *
     * This function is called when event 'centre-view' is emitted by child scopes.
     *
     * This event is emitted by the following child directives:
     * <ul>
     *   <li>centreSummaryDirective</li>
     *   <li>centreStudiesPanelDirective</li>
     *   <li>locationsPanelDirective</li>
     * </ul>
     */
    function activeTabUpdate(event) {
      event.stopPropagation();
      _.each(vm.tabs, function (tab, index) {
        tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
        if (tab.active) {
          vm.active = index;
        }
      });
    }
  }

  return centreViewDirective;
});
