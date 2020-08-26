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
    'ui.router',
    'base64',
    'ngStorage',
    'cp.ngConfirm'
  ])
  
  .value('urlPrefix', '/backend')
  
  .config(['$stateProvider', '$urlRouterProvider', '$httpProvider',
           function ($stateProvider, $urlRouterProvider, $httpProvider) {
	 
	 $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
	 $httpProvider.interceptors.push('httpInterceptor');
	 
	 $urlRouterProvider.otherwise('/order');

	 $stateProvider
      .state('order', {
    	  url:'/order',
    	  controller: 'OrderCtrl',
    	  templateUrl:'order.html'
    })
    
    
   /* .state('main.order',{
        url:'/order',
        controller: 'OrderCtrl',
        templateUrl:'index.html',
        resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['orderCtrl.js']
              });
            }
        }
    })*/
}]);


