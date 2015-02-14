define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('spcLinkAnnotTypesPanel', spcLinkAnnotTypesPanel);

  /**
   *
   */
  function spcLinkAnnotTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        annotTypes: '=',
        spcLinkTypes: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/spcLinkAnnotTypesPanel.html',
      controller: 'SpcLinkAnnotTypesPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('SpcLinkAnnotTypesPanelCtrl', SpcLinkAnnotTypesPanelCtrl);

  SpcLinkAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'spcLinkAnnotTypesService',
    'spcLinkAnnotTypeRemoveService',
    'Panel',
    'AnnotationTypeViewer'
  ];

  /**
   * A panel to display a study's specimen link annotation types.
   */
  function SpcLinkAnnotTypesPanelCtrl($scope,
                                      $state,
                                      modalService,
                                      spcLinkAnnotTypesService,
                                      spcLinkAnnotTypeRemoveService,
                                      Panel,
                                      AnnotationTypeViewer) {
    var vm = this;

    var helper = new Panel(
      'study.panel.specimenLinkAnnotationTypes',
      'home.admin.studies.study.processing.spcLinkAnnotTypeAdd');

    vm.study            = $scope.study;
    vm.annotTypes       = $scope.annotTypes;
    vm.spcLinkTypes     = $scope.spcLinkTypes;
    vm.update           = update;
    vm.remove           = remove;
    vm.information      = information;
    vm.add              = add;
    vm.panelOpen        = helper.panelOpen;
    vm.panelToggle      = panelToggle;

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = helper.getTableParams(vm.annotTypes);

    vm.annotTypesInUse = annotTypesInUse();

    //--

    function add() {
      return helper.add();
    }

    function information(annotType) {
      return new AnnotationTypeViewer(annotType, 'Specimen Link Annotation Type');
    }

    function panelToggle() {
      vm.panelOpen = helper.panelToggle();
      return vm.panelOpen;
    }

    /**
     * Returns the annotation types that are in use.
     *
     */
    function annotTypesInUse() {
      var result = {};
      _.each(vm.spcLinkTypes, function(slt) {
        _.each(slt.annotationTypeData, function (atItem) {
          // push the value as a key
          result[atItem.annotationTypeId] = true;
        });
      });
      return _.keys(result);
    }

    function annotTypeInUseModal() {
      var headerHtml = 'Cannot update this annotation type';
      var bodyHtml = 'This annotation type is in use by a specimen link type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the specimen link type(s) that use it.';
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    /**
     * Switches state to update a specimen link annotation type.
     */
    function update(annotType) {
      if (_.contains(vm.annotTypesInUse, annotType.id)) {
        annotTypeInUseModal();
      } else {
        $state.go(
          'home.admin.studies.study.processing.spcLinkAnnotTypeUpdate',
          { annotTypeId: annotType.id });
      }
    }

    function remove(annotType) {
      spcLinkAnnotTypeRemoveService.remove(annotType, vm.annotTypesInUse);
    }

  }

});
