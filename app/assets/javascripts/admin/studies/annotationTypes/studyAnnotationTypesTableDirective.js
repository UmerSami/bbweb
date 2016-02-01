/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function studyAnnotationTypesTableDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study:                  '=',
        annotationTypes:        '=',
        annotationTypeIdsInUse: '=',
        annotationTypeName:     '=',
        updateStateName:        '=',
        hasRequired:            '@'
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypesPanel.html',
      controller: 'StudyAnnotationTypesTableCtrl as vm'
    };
  }

  StudyAnnotationTypesTableCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'studyAnnotationTypeUtils',
    'AnnotationTypeViewer'
  ];

  /**
   * A table to display a study's participant annotation types.
   */
  function StudyAnnotationTypesTableCtrl($scope,
                                         $state,
                                         modalService,
                                         studyAnnotationTypeUtils,
                                         AnnotationTypeViewer) {
    var vm = this;

    vm.study                  = $scope.study;
    vm.annotationTypes        = angular.copy($scope.annotationTypes);
    vm.annotationTypeIdsInUse = $scope.annotationTypeIdsInUse;
    vm.annotationTypeName     = $scope.annotationTypeName;
    vm.updateStateName        = $scope.updateStateName;
    vm.hasRequired            = $scope.hasRequired;
    vm.update                 = update;
    vm.remove                 = remove;
    vm.information            = information;
    vm.modificationsAllowed   = vm.study.isDisabled();

    vm.columns = annotationTypeColumns($scope.annotationTypeName);

    //--

    /**
     * Order is important here.
     */
    function annotationTypeColumns(annotationTypeName) {
      var result =  [
        { title: 'Name', field: 'name' },
        { title: 'Type', field: 'valueType' }
      ];

      if (annotationTypeName === 'ParticipantAnnotationType') {
        result.push({ title: 'Required', field: 'required' });
      }

      result.push({ title: 'Description', field: 'description' });

      return result;
    }

    function information(annotationType) {
      return new AnnotationTypeViewer(annotationType, vm.annotationTypeName);
    }

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotationType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.annotationTypeIdsInUse, annotationType.id)) {
        studyAnnotationTypeUtils.updateInUseModal(annotationType);
      } else {
        $state.go(vm.updateStateName, { annotationTypeId: annotationType.id });
      }
    }

    function remove(annotationType) {
      if (_.contains(vm.annotationTypeIdsInUse, annotationType.id)) {
        studyAnnotationTypeUtils.removeInUseModal(annotationType);
      } else {
        if (!vm.study.isDisabled()) {
          throw new Error('study is not disabled');
        }
        studyAnnotationTypeUtils.remove(annotationType)
          .then(function () {
            vm.annotationTypes = _.without(vm.annotationTypes, annotationType);
          });
      }
    }

  }

  return {
    directive: studyAnnotationTypesTableDirective,
    controller: StudyAnnotationTypesTableCtrl
  };
});
