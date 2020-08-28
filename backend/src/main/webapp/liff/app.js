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
	 
	 $urlRouterProvider.otherwise('/home/order');

	 $stateProvider
	 .state('home', {
		 url:'/home',
		 controller: function($scope, $state) {
			 $scope.goHome = function() {
				 $state.go("home.order");
			 }
		 },
		 templateUrl:'home.html'
	 })
      .state('home.order', {
    	  url:'/order',
    	  controller: 'OrderCtrl',
    	  templateUrl:'order.html'
      })
      .state('home.order.showOrder', {
    	  url:'/showOrder',
    	  params: {createdDateTime: null},
    	  controller: 'ShowOrderCtrl',
    	  templateUrl:'showOrder/main.html'
      })
    
    
}]);


