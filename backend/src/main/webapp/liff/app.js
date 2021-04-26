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
    'cp.ngConfirm',
    'ui.bootstrap'
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
		 controller: function($scope, $state, $timeout) {
			 $scope.goHome = function() {
				 if(!$scope.isOverTime) {
					 $state.go("home.order");
					 
					 $timeout(function() {						 
						 $scope.$$childHead.initSwipe();
					 }, 500);
				 }
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
    	  params: {createdDateTime: null, restrictList: null, sendRoundData: null, isOverTime: null},
    	  controller: 'ShowOrderCtrl',
    	  templateUrl:'showOrder/main.html'
      })
      .state('home.order.showOrder.lottoResult',{
        templateUrl:'result/main.html',
        url:'/lottoResult',
        controller: 'LottoResultCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['result/lottoResultCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	if($rootScope.userId) {
            		return $http.get(urlPrefix + '/restAct/order/getPeriod?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
            			if(data.data.statusCode != 9999) {
            				$rootScope.systemAlert(data.data.statusCode);
            				return $q.reject(data);
            			}
            			
            			return data.data;
            		}, function(response) {
            			$rootScope.systemAlert(response.status);
            		});            		
            	} else {
            		return null;
            	}
            }
    	}
    })
    
    
}]);


