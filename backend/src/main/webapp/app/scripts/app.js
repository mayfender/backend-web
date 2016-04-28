'use strict';
/**
 * @ngdoc overview
 * @name sbAdminApp
 * @description
 * # sbAdminApp
 *
 * Main module of the application.
 */
angular
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
    'ngStomp',
    'naif.base64',
    'xeditable',
    'checklist-model',
    'colorpicker.module'
  ])
  
  .run(function(editableOptions) {
	  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
  })
  
  .value('urlPrefix', '/backend') //-------- '/ricoh' or ''
  
  .value('roles', [{authority:'ROLE_STAFF', name:'Staff'}, {authority:'ROLE_ADMIN', name:'Admin'}])
  
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider', '$httpProvider', '$translateProvider',
           function ($stateProvider,$urlRouterProvider,$ocLazyLoadProvider, $httpProvider, $translateProvider) {
	 
	 $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
	 $httpProvider.interceptors.push('httpInterceptor');
	  
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

    $urlRouterProvider.otherwise('/dashboard/table_land');

    $stateProvider
      .state('dashboard', {
        url:'/dashboard',
        templateUrl: 'views/dashboard/main.html',
        resolve: {
            loadMyDirectives:function($ocLazyLoad){
                return $ocLazyLoad.load(
                {
                    name:'sbAdminApp',
                    files:[
                    'scripts/directives/header/header.js',
                    'scripts/directives/header/header-notification/header-notification.js',
                    'scripts/directives/sidebar/sidebar.js',
                    'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                    ]
                })/*,
                $ocLazyLoad.load(
                {
                   name:'toggle-switch',
                   files:["bower_components/angular-toggle-switch/angular-toggle-switch.min.js",
                          "bower_components/angular-toggle-switch/angular-toggle-switch.css"
                      ]
                }),
                $ocLazyLoad.load(
                {
                  name:'ngAnimate',
                  files:['bower_components/angular-animate/angular-animate.js']
                })
                $ocLazyLoad.load(
                {
                  name:'ngCookies',
                  files:['bower_components/angular-cookies/angular-cookies.js']
                })
                $ocLazyLoad.load(
                {
                  name:'ngResource',
                  files:['bower_components/angular-resource/angular-resource.js']
                })
                $ocLazyLoad.load(
                {
                  name:'ngSanitize',
                  files:['bower_components/angular-sanitize/angular-sanitize.js']
                })
                $ocLazyLoad.load(
                {
                  name:'ngTouch',
                  files:['bower_components/angular-touch/angular-touch.js']
                })*/
            }
        }
    })
      .state('dashboard.home',{
        url:'/home',
        controller: 'MainCtrl',
        templateUrl:'views/dashboard/home.html',
        resolve: {
          loadMyFiles:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'sbAdminApp',
              files:[
              'scripts/controllers/main.js',
              'scripts/directives/timeline/timeline.js',
              'scripts/directives/notifications/notifications.js',
              'scripts/directives/chat/chat.js',
              'scripts/directives/dashboard/stats/stats.js'
              ]
            })
          }
        }
      })
    //------------------------------------: User :-------------------------------------------
    .state('dashboard.user',{
        templateUrl:'views/user/main.html',
    	controller: function($scope, $state){
    		$scope.itemsPerPage = 10;
    		$scope.formData = {currentPage : 1};
    		$scope.formData.status;
    		$scope.formData.role;
    		$scope.formData.userName;
    		
    		$scope.gotoSelected = function() {
    			$state.go("dashboard.user." + $scope.url, {
    				'itemsPerPage': $scope.itemsPerPage, 
    				'currentPage': $scope.formData.currentPage,
    				'status': $scope.formData.status, 
    				'role': $scope.formData.role, 
    				'userName': $scope.formData.userName
    			});
    		}
    	}
    })
    .state('dashboard.user.search',{
    	templateUrl:'views/user/search.html',
    	url:'/user/search',
    	params: {'itemsPerPage': 10, 'currentPage': 1, 'status': null, 'role': null, 'userName': null},
    	controller: 'SearchUserCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/user/searchUserCtrl.js']
              });
            },
            loadUsers:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/user/findUserAll', {
		            		userName: $stateParams.userName,
		        			role: $stateParams.role,
		        			status: $stateParams.status,
		        			currentPage: $stateParams.currentPage,
		        	    	itemsPerPage: $stateParams.itemsPerPage
            			}).then(function(data){
		            		if(data.data.statusCode != 9999) {
		            			$rootScope.systemAlert(data.data.statusCode);
		            			return $q.reject(data);
		            		}
            		
		            		return data.data;
		            	}, function(response) {
		            		$rootScope.systemAlert(response.status);
		        	    });
            }
    	}
    })
    .state('dashboard.user.add',{
    	templateUrl:'views/user/add.html',
    	url:'/user/add',
    	params: {'user': null},
    	controller: 'AddUserCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/user/addUserCtrl.js']
              });
            }
    	}
    })
    //------------------------------------: Profile :-------------------------------------------
    .state('dashboard.profile',{
        templateUrl:'views/profile/main.html',
        url:'/profile',
    	controller: "ProfileCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/profileCtrl.js']
              });
            },
            loadProfile:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/user/loadProfile?userName=' + $rootScope.principal.username).then(function(data){
		            		if(data.data.statusCode != 9999) {
		            			$rootScope.systemAlert(data.data.statusCode);
		            			return $q.reject(data);
		            		}
            		
		            		return data.data;
		            	}, function(response) {
		            		$rootScope.systemAlert(response.status);
		        	    });
            }
    	}
    })
    //------------------------------------: Menu :-------------------------------------------
    .state('dashboard.menu',{
        templateUrl:'views/menu/main.html',
        controller: function($scope, $state, $log){
        	$scope.itemsPerPage = 10;
    		$scope.formData = {currentPage : 1};
    		
        	$scope.gotoSelected = function() {        		
    			$state.go("dashboard.menu." + $scope.url, {
    				itemsPerPage: $scope.itemsPerPage, 
    				currentPage: $scope.formData.currentPage,
    				name: $scope.formData.name,
    				status: $scope.formData.status,
    				isRecommented: $scope.formData.isRecommented,
    				menuTypeId: $scope.formData.menuTypeId
    			});
    		}
        }
    })
    .state('dashboard.menu.search',{
        templateUrl:'views/menu/search.html',
        url:'/menu/search',
        params: {'itemsPerPage': 10, 'currentPage': 1, 'name': null, 'status': null, 'isRecommented': null, 'menuTypeId': null},
        controller: 'SearchMenuCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/menu/searchMenuCtrl.js']
              });
            },
            loadAllMenu:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/menu/searchMenu', {
            		name: $stateParams.name,
            		status: $stateParams.status,
            		isRecommented: $stateParams.isRecommented,
            		menuTypeId: $stateParams.menuTypeId,
        			currentPage: $stateParams.currentPage,
        	    	itemsPerPage: $stateParams.itemsPerPage
            	}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
    	}
    })
    .state('dashboard.menu.add',{
        templateUrl:'views/menu/add.html',
        url:'/menu/add',
        params: {'menu': null},
        controller: 'AddMenuCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/menu/addMenuCtrl.js']
              });
            },
            loadImg:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	if(!$stateParams.menu) return null;
            	
            	var imgId = $stateParams.menu.image.id || "";
            	
            	return $http.get(urlPrefix + '/restAct/menu/editData?imgId=' + imgId +'&menuTypeId=' + $stateParams.menu.menuType.id).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
    	}
    })
    //------------------------------------: Menu Type :-------------------------------------------
    .state('dashboard.menu_type',{
        templateUrl:'views/menu_type/main.html',
        url:'/menuType',
        controller: 'MenuTypeCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/menu_type/menuTypeCtrl.js']
        		});
        	},
        	loadMenuType:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/menuType/loadMenuType').then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })
    //------------------------------------: Sale :-------------------------------------------
    .state('dashboard.sale',{
        templateUrl:'views/sale/main.html',
        controller: 'SaleMainCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/sale/saleMainCtrl.js']
        		});
        	}
        }
    })
    .state('dashboard.sale.search',{
        templateUrl:'views/sale/search.html',
        url:'/sale/search',
        controller: 'SaleSearchCtrl',
        params: {'ref': null, 'status': null},
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:[
        			       'scripts/controllers/sale/saleSearchCtrl.js',
        			       'scripts/directives/sale/sale.js'
        			]
        		});
        	},
        	loadCus:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/customer/searchCus', {
            		ref: $stateParams.ref,
            		status: $stateParams.status
            	}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    }).state('dashboard.sale.detail',{
        templateUrl:'views/sale/detail.html',
        url:'/sale/detail',
        controller: 'SaleDetailCtrl',
        params: {'cusId': null, 'tableDetail': null, 'ref': null, 'status': null, 'receiveAmount': null, 'changeCash': null, 'totalPrice': null},
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:[
        			       'scripts/controllers/sale/saleDetailCtrl.js'
        			]
        		});
        	},
        	loadOrders:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/order/findOrderByCus?cusId=' + $stateParams.cusId).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })
    //------------------------------------: Kitchen :-------------------------------------------
    .state('dashboard.kitchen',{
        templateUrl:'views/kitchen/main.html',
        controller: function($scope, $state, $log){
        	$scope.formData = {isDetailMode: false};
        	
        	$scope.gotoSelected = function() {        		
    			$state.go("dashboard.kitchen.search", {
    				ref: $scope.formData.ref,
    				status: $scope.formData.status
    			});
    		}
        }
    })
    .state('dashboard.kitchen.search',{
        templateUrl:'views/kitchen/search.html',
        url:'/kitchen/search',
        controller: 'KitchenSearchCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/kitchen/kitchenSearchCtrl.js']
        		});
        	},
        	loadOrder:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/order/searchOrder').then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })
    //------------------------------------: Report :-------------------------------------------
    .state('dashboard.report',{
        templateUrl:'views/report/main.html',
        controller: function($scope, $state, $log){
        	$scope.searchForm = {reportDate: new Date()};
        	
        	$scope.gotoSelected = function() {        		
    			$state.go("dashboard.report.money", $scope.searchForm);
    		}
        }
    })
    .state('dashboard.report.money',{
        templateUrl:'views/report/money.html',
        url:'/report/money',
        params: {reportDate: new Date()},
        controller: 'ReportMoneyCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/report/moneyCtrl.js', 'scripts/directives/report/money.js']
        		});
        	},
        	loadReport:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/report/money', {reportDate: $stateParams.reportDate}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })
    .state('dashboard.report.menu',{
        templateUrl:'views/report/menu.html',
        url:'/report/menu',
        params: {'reportDate': null},
        controller: 'ReportMenuCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/report/menuCtrl.js']
        		});
        	},
        	loadReport:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/report/menu', {reportDate: $stateParams.reportDate}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })
    
    //------------------------------------: Form :-------------------------------------------
      .state('dashboard.form',{
        templateUrl:'views/form.html',
        url:'/form'
    })
      .state('dashboard.blank',{
        templateUrl:'views/pages/blank.html',
        url:'/blank'
    })
      .state('login',{
        templateUrl:'views/login.html',
        params: {'action': null},
        url:'/login',
        controller: 'LoginCtrl'
    })
      .state('dashboard.chart',{
        templateUrl:'views/chart.html',
        url:'/chart',
        controller:'ChartCtrl',
        resolve: {
          loadMyFile:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'chart.js',
              files:[
                'bower_components/angular-chart.js/dist/angular-chart.min.js',
                'bower_components/angular-chart.js/dist/angular-chart.css'
              ]
            }),
            $ocLazyLoad.load({
                name:'sbAdminApp',
                files:['scripts/controllers/chartContoller.js']
            })
          }
        }
    })
      .state('dashboard.table',{
        templateUrl:'views/table.html',
        url:'/table'
    })
      .state('dashboard.panels-wells',{
          templateUrl:'views/ui-elements/panels-wells.html',
          url:'/panels-wells'
      })
      .state('dashboard.buttons',{
        templateUrl:'views/ui-elements/buttons.html',
        url:'/buttons'
    })
      .state('dashboard.notifications',{
        templateUrl:'views/ui-elements/notifications.html',
        url:'/notifications'
    })
      .state('dashboard.typography',{
       templateUrl:'views/ui-elements/typography.html',
       url:'/typography'
   })
      .state('dashboard.icons',{
       templateUrl:'views/ui-elements/icons.html',
       url:'/icons'
   })
      .state('dashboard.grid',{
       templateUrl:'views/ui-elements/grid.html',
       url:'/grid'
   })
   
   
   //------------------------------------: Table Land :-------------------------------------------
   /* .state('dashboard.table_land',{
        templateUrl:'views/table_land/main.html',
        controller: function($scope, $state, $log){
        	$scope.formData = {isEditMode: false};
        	
        	$scope.gotoSelected = function() {        		
    			$state.go("dashboard.table_land.search", {
    				name: $scope.formData.tableName,
    				status: $scope.formData.status
    			});
    		}
        }
    })
    .state('dashboard.table_land.search',{
        templateUrl:'views/table_land/search.html',
        url:'/tableLand/search',
        controller: 'TableLandSearchCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:[
        			       'scripts/controllers/table_land/tableLandSearchCtrl.js',
        			       'scripts/directives/table_land/table.js'
        			]
        		});
        	},
        	loadTables:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/table/searchTable', {
            		
            	}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    }).state('dashboard.table_land.detail',{
        templateUrl:'views/table_land/detail.html',
        url:'/tableLand/detail',
        controller: 'TableLandDetailCtrl',
        params: {'tableId': 19},
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:[
        			       'scripts/controllers/table_land/tableLandDetailCtrl.js'
        			]
        		});
        	},
        	loadOrders:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/order/searchOrder', {
            		tableId: $stateParams.tableId
            	}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })*/
    //------------------------------------: Table Manage :-------------------------------------------
    /* .state('dashboard.table_manage',{
        templateUrl:'views/table_manage/main.html',
        url:'/tableManage',
        controller: 'TableManageCtrl',
        resolve: {
        	loadMyFiles:function($ocLazyLoad) {
        		return $ocLazyLoad.load({
        			name:'sbAdminApp',
        			files:['scripts/controllers/table_manage/tableManageCtrl.js']
        		});
        	},
        	loadTables:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/table/searchTable',{}).then(function(data){
            		if(data.data.statusCode != 9999) {
            			$rootScope.systemAlert(data.data.statusCode);
            			return $q.reject(data);
            		}
            		
	        		return data.data;
	        	}, function(response) {
	        		$rootScope.systemAlert(response.status);
	    	    });
            }
        }
    })*/
   
   
   
}]);