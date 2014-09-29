/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('users.services', ['biobank.common', 'ngCookies']);

  mod.factory('userService', [
    '$http', '$q', '$cookies', '$log', 'BbwebRestApi',
    function($http, $q, $cookies, $log, BbwebRestApi) {
      var user, token = $cookies['XSRF-TOKEN'];

      /* If the token is assigned, check that the token is still valid on the server */
      if (token) {
        $http.get('/authenticate').then(
          function(response) {
            user = response.data.data;
            $log.info('Welcome back, ' + user.name);
          },
          function() {
            /* the token is no longer valid */
            $log.info('Token no longer valid, please log in.');
            token = undefined;
            delete $cookies['XSRF-TOKEN'];
            return $q.reject('Token invalid');
          });
      }

      var changeStatus = function(user, status) {
        var cmd = {
          id: user.id,
          expectedVersion: user.version
        };
        return BbwebRestApi.call('POST', '/users/' + status, cmd);
      };

      return {
        login: function(credentials) {
          return $http.post('/login', credentials).then(function(response) {
            token = response.data.token;
            return $http.get('/authenticate');
          }).then(function(response) {
            user = response.data.data;
            $log.info('Welcome ' + user.name);
            return user;
          });
        },
        logout: function() {
          // Logout on server in a real app
          return $http.post('/logout').then(function() {
            $log.info('Good bye');
            delete $cookies['XSRF-TOKEN'];
            token = undefined;
            user = undefined;
          });
        },
        getUser: function() {
          return user;
        },
        query: function(userId) {
          return BbwebRestApi.call('GET', '/users/' + userId);
        },
        getAllUsers: function() {
          return BbwebRestApi.call('GET', '/users');
        },
        getUsers: function(query, sort, order) {
          return BbwebRestApi.call(
            'GET',
            '/users?' + query + '&sort=' + sort + '&order=' + order);
        },
        add: function(newUser) {
          var cmd = {
            name:     newUser.name,
            email:    newUser.email,
            password: newUser.password
          };
          if (newUser.avatarUrl) {
            cmd.avatarUrl = newUser.avatarUrl;
          }
          return BbwebRestApi.call('POST', '/users', cmd);
        },
        update: function(user, newPassword) {
          var cmd = {
            expectedVersion: user.version,
            name:            user.name,
            email:           user.email
          };

          if (user.password) {
            cmd.password = newPassword;
          }

          if (user.avatarUrl) {
            cmd.avatarUrl = user.avatarUrl;
          }
          return BbwebRestApi.call('PUT', '/users/' + user.id, cmd);
        },
        passwordReset: function(email) {
          return BbwebRestApi.call('POST', '/passreset', { email: email });
        },
        activate: function(user) {
          return changeStatus(user, 'activate');
        },
        lock: function(user) {
          return changeStatus(user, 'lock');
        },
        unlock: function(user) {
          return changeStatus(user, 'unlock');
        }
      };
    }
  ]);

  /**
   * Add this object to a route definition to only allow resolving the route if the user is
   * logged in. This also adds the contents of the objects as a dependency of the controller.
   */
  mod.constant('userResolve', {
    user: ['$cookies', '$q', '$http', 'userService', function($cookies, $q, $http, userService) {
      var token;
      var deferred = $q.defer();
      var user = userService.getUser();

      if (user) {
        deferred.resolve(user);
      } else {
        token = $cookies['XSRF-TOKEN'];

        if (token) {
          $http.get('/authenticate').then(
            function(response) {
              deferred.resolve(response.data.data);
            },
            function(response) {
              deferred.reject(response.data);
            });
        } else {
          deferred.reject();
        }
      }
      return deferred.promise;
    }]
  });

  /**
   * If the current route does not resolve, go back to the start page.
   */
  var handleRouteError = ['$rootScope', '$state', function($rootScope, $state) {
    $rootScope.$on('$routeChangeError', function() {
      $state.go('home');
    });
  }];

  handleRouteError.$inject = ['$rootScope', '$state'];
  mod.run(handleRouteError);
  return mod;
});