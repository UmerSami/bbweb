/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (){
  'use strict';


  /**
   * Displays a right justified button with a 'plus' icon. Meant to be used in a pane to add a
   * domain object.
   */
  function panelButtonsDirective() {
    var directive = {
      restrict: 'E',
      replace: true,
      scope: {
        onAdd: '&',
        addButtonTitle: '@',
        addButtonEnabled: '&',
        panelOpen: '='
      },
      templateUrl: '/assets/javascripts/common/directives/panelButtons.html',
      controller: PanelButtonsController,
      controllerAs: 'vm'
    };
    return directive;
  }

  PanelButtonsController.$inject =  ['$scope'];

  /**
   * This controller is needed to stop the propagation of the event generated when the 'Add' button
   * is pressed. If the event was allowed to propgate, then the panel would be collapsed.
   */
  function PanelButtonsController($scope) {
    var vm = this;

    vm.addButtonPressed = addButtonPressed;

    function addButtonPressed($event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.onAdd();
    }
  }

  return panelButtonsDirective;
});
