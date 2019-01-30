
'use strict';
/**
 * @ngdoc overview
 * @name sbAdminApp
 * @description
 * # sbAdminApp
 *
 * Main module of the application.
 */
var app = angular
  .module('sbAdminApp', [
    'oc.lazyLoad',
    'ngAnimate',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
    'ngSanitize',
    'base64',
    'toaster',
    'pascalprecht.translate',
    'ngStorage',
  ])
  
  .value('urlPrefix', '/backend')
  
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider', '$httpProvider', '$translateProvider', 'cfpLoadingBarProvider',
           function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, $httpProvider, $translateProvider, cfpLoadingBarProvider) {
	 
	 $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
	 $httpProvider.interceptors.push('httpInterceptor');
	 cfpLoadingBarProvider.spinnerTemplate = '<div id="loading-bar-spinner"><i class="fa fa-spinner fa-spin fa-fw"></i></div>';
	 
	 $ocLazyLoadProvider.config({
	      debug:false,
	      events:true,
	 });
	 
	//-------: i18n
	$translateProvider.useStaticFilesLoader({
        prefix: 'i18n/locale-',
        suffix: '.json'
    });
	$translateProvider.preferredLanguage('th');
	$translateProvider.useSanitizeValueStrategy(null);

    $urlRouterProvider.otherwise('/manual/form');

    $stateProvider
      .state('manual', {
        url:'/manual',
        templateUrl: 'views/manual/main.html',
        resolve: {
            loadMyDirectives:function($ocLazyLoad){
                return $ocLazyLoad.load(
                {
                    name:'sbAdminApp',
                    files:[
                    'scripts/directives/header2/header.js',
                    'scripts/directives/sidebar2/sidebar.js'
                    ]
                })
            }
        }
    })
    //------------------------------------: Form :-------------------------------------------
      .state('manual.form',{
        templateUrl:'views/form.html',
        url:'/form'
    })
      .state('manual.blank',{
        templateUrl:'views/pages/blank.html',
        url:'/blank'
    })
    .state('manual.table',{
        templateUrl:'views/table.html',
        url:'/table'
    })
      .state('manual.panels-wells',{
          templateUrl:'views/ui-elements/panels-wells.html',
          url:'/panels-wells'
      })
      .state('manual.buttons',{
        templateUrl:'views/ui-elements/buttons.html',
        url:'/buttons'
    })
      .state('manual.notifications',{
        templateUrl:'views/ui-elements/notifications.html',
        url:'/notifications'
    })
      .state('manual.typography',{
       templateUrl:'views/ui-elements/typography.html',
       url:'/typography'
   })
      .state('manual.icons',{
       templateUrl:'views/ui-elements/icons.html',
       url:'/icons'
   })
      .state('manual.grid',{
       templateUrl:'views/ui-elements/grid.html',
       url:'/grid'
   })
}]);