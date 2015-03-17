/**
 * Configure routes of user module.
 */
define([], function() {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {
    $urlRouterProvider.otherwise('/');

    /**
     * Prticipant Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.participants.annotTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            required: false,
            valueType: '',
            options: []
          };
        }],
        addOrUpdateFn: ['participantAnnotTypesService', function(participantAnnotTypesService) {
          return participantAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type'
      }
    });

    /**
     * Prticipant Annotation Type Update
     */
    $stateProvider.state('home.admin.studies.study.participants.annotTypeUpdate', {
      url: '/annottype/update/{annotTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: [
          '$stateParams', 'participantAnnotTypesService', 'study',
          function($stateParams, participantAnnotTypesService, study) {
            if ($stateParams.annotTypeId) {
              return participantAnnotTypesService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        childReturnState: function() {
          return 'home.admin.studies.study.participants';
        },
        addOrUpdateFn: ['participantAnnotTypesService', function(participantAnnotTypesService) {
          return participantAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type'
      }
    });
  }

  return config;
});
