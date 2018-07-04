'use strict';

// Declare app level module which depends on views, and components
angular
  .module('myApp'
  , ['ui.router'
    , 'myApp.search'
    , 'myApp.version'
    , 'angular-loading-bar'
  ])
  .config(function ($stateProvider, $urlRouterProvider, $locationProvider) {
    $locationProvider.hashPrefix('');
    // For any unmatched url, redirect to /state1
    $urlRouterProvider.otherwise("/search");
  });

