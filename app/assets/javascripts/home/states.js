/**
 * Home module routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/index');

    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': {
            template: '<home user="vm.user"></home>',
            controller: [function () {
            }],
            controllerAs: 'vm'
          }
        },
        data: {
          displayName: 'Home'
        }
      })
      .state('home.about', {
        url: 'about',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/about.html'
          }
        },
        data: {
          displayName: 'About'
        }
      })
      .state('home.contact', {
        url: 'contact',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/contact.html'
          }
        },
        data: {
          displayName: 'Contact Us'
        }
      });
  }

  return config;
});
