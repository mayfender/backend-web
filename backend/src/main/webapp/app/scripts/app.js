
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
    'ngStomp',
    'ngCookies',
    'ngStorage',
    'angularFileUpload',
    'dndLists',
    'sticky',
    'xeditable',
    'ngContextMenu',
    'naif.base64',
    'dnTimepicker',
    'dateParser',
    'selectize',
    'ngTagsInput',
    'colorpicker.module'
  ])
  
  .run(function(editableOptions) {
	  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
  })
  
  .value('urlPrefix', '/backend')
  
  .value('roles', [{authority:'ROLE_SUPERADMIN', name:'Superadmin'},
                   {authority:'ROLE_MANAGER', name:'Manager'},
                   {authority:'ROLE_ADMIN', name:'Admin'},
                   {authority:'ROLE_SUPERVISOR', name:'Supervisor'},
                   {authority:'ROLE_USER', name:'User'}])
                   
  .value('roles2', [{authority:'ROLE_SUPERVISOR', name:'Supervisor'},{authority:'ROLE_USER', name:'User'}])
  .value('roles3', [{authority:'ROLE_MANAGER', name:'Manager'},{authority:'ROLE_ADMIN', name:'Admin'}])
  
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider', '$httpProvider', '$translateProvider', 'cfpLoadingBarProvider', 'tagsInputConfigProvider',
           function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, $httpProvider, $translateProvider, cfpLoadingBarProvider, tagsInputConfigProvider) {
	 
	 $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
	 $httpProvider.interceptors.push('httpInterceptor');
	 cfpLoadingBarProvider.spinnerTemplate = '<div id="loading-bar-spinner"><i class="fa fa-spinner fa-spin fa-fw"></i></div>';
	 tagsInputConfigProvider.setDefaults('tagsInput', {
	      minLength: 1
	 });
	 
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

    $urlRouterProvider.otherwise('/login');

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
    .state('dashboard.summaryReport',{
        url:'/summaryReport',
        controller: 'DashBoard',
        templateUrl:'views/dashboard/home.html',
        resolve: {
          loadMyFiles:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'sbAdminApp',
              files:[
              'scripts/controllers/dashboard/dashBoard.js',
              'scripts/directives/timeline/timeline.js',
              'scripts/directives/notifications/notifications.js',
              'scripts/directives/chat/chat.js',
              'scripts/directives/dashboard/stats/stats.js',
              'scripts/directives/datepicker/datepicker.js'
              ]
            }),
            $ocLazyLoad.load({
                name:'chart.js',
                files:[
					'lib/angular-chart.min.js'
                ]
              })
          }
        }
      })
      .state('dashboard.summaryReport.collector',{
    	  url:'/summaryReport/collector',
          controller: 'Collector',
          templateUrl:'views/dashboard/collector.html',
          resolve: {
              loadMyFiles:function($ocLazyLoad) {
                return $ocLazyLoad.load({
                  name:'sbAdminApp',
                  files:[
                  'scripts/controllers/dashboard/collector.js'
                  ]
                })
              }
          }
      })
    .state('dashboard.dictionary',{
        templateUrl:'views/dictionary.html',
        url:'/dictionary',
        controller: function($scope, $http) {
        	$scope.translate = function() {
        		 $http.jsonp('https://glosbe.com/gapi/translate?tm=false&from=eng&dest=th&format=json&phrase='+ $scope.source.trim().toLowerCase() +'&callback=JSON_CALLBACK&pretty=true')
        	        .then(function(data){
        	        	$scope.phrases = data.data.tuc;
        	        }, function(response) {
        	        	$rootScope.systemAlert(response.status);
        	        });	
        	}
        }
    })
    .state('dashboard.setting',{
        templateUrl:'views/setting/main.html',
        url:'/setting',
        controller: 'SettingCtrl',
        resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/setting/settingCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/setting/getData').then(function(data){
		            		if(data.data.statusCode != 9999) {
		            			if(data.data.statusCode == 6000) {		            				
		            				$state.go("login");
		            				return;
		            			}
		            			
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
    //------------------------------------: Dynamic List :-------------------------------------------
    .state('dashboard.dymList',{
        templateUrl:'views/dym_list/main.html',
    	controller: function($scope, $state){
    		$scope.gotoSelected = function() {
    			$scope.isShowBack = false;
    			$scope.isShowProd = true;
    			$state.go("dashboard.dymList.list");
    		}
    	}
    })
    .state('dashboard.dymList.list',{
    	templateUrl:'views/dym_list/list.html',
    	url:'/dyList/list',
    	controller: 'DymListListCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/dym_list/listCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/dymList/findList', {
						productId: $rootScope.workingOnProduct.id,
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
    .state('dashboard.dymList.list.listDet',{
    	templateUrl:'views/dym_list/list_det.html',
    	url:'/det',
    	params: {id: null, productId: null},
    	controller: 'DymListDetCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/dym_list/listDetCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/dymList/findListDet', {
            			dymListId: $stateParams.id,
						productId: $stateParams.productId
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
    //------------------------------------: User :-------------------------------------------
    .state('dashboard.user',{
        templateUrl:'views/user/main.html',
    	controller: function($rootScope, $scope, $state, loadProducts){
    		$scope.itemsPerPage = 10;
    		$scope.formData = {currentPage : 1};
    		$scope.formData.enabled;
    		$scope.formData.role;
    		$scope.formData.userName;
    		$scope.formData.product = $rootScope.workingOnProduct.id;
    		$scope.productsSelect = loadProducts.products;
    		
    		$scope.gotoSelected = function() {
    			$state.go("dashboard.user." + $scope.url, {
    				'itemsPerPage': $scope.itemsPerPage, 
    				'currentPage': $scope.formData.currentPage,
    				'enabled': $scope.formData.enabled, 
    				'role': $scope.formData.role, 
    				'userName': $scope.formData.userName,
    				'product': $scope.formData.product
    			});
    		}
    	},
    	resolve: {
            loadProducts:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/product/findProduct', {
		        			enabled: 1,
		        			currentPage: 1,
		        	    	itemsPerPage: 1000
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
    .state('dashboard.user.search',{
    	templateUrl:'views/user/search.html',
    	url:'/user/search',
    	params: {'itemsPerPage': 10, 'currentPage': 1, 'enabled': null, 'role': null, 'userName': null, 'product': null},
    	controller: 'SearchUserCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/user/searchUserCtrl.js']
              });
            },
            loadUsers:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/user/findUserAll', {
		            		userName: $stateParams.userName,
		        			role: $stateParams.role,
		        			enabled: $stateParams.enabled,
		        			currentPage: $stateParams.currentPage,
		        	    	itemsPerPage: $stateParams.itemsPerPage,
		        	    	currentProduct: $rootScope.workingOnProduct.id,
		        	    	product: $stateParams.product || $rootScope.workingOnProduct.id
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
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/user/editUser', {
            				enabled: 1,
		        			currentPage: 1,
		        	    	itemsPerPage: 1000,
		        	    	productName: '',
		        	    	userId: $stateParams.user && $stateParams.user.id
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
    //------------------------------------: Product :-------------------------------------------
    .state('dashboard.product',{
        templateUrl:'views/product/main.html',
    	controller: function($scope, $state){
    		$scope.itemsPerPage = 10;
    		$scope.formData = {currentPage : 1};
    		$scope.formData.enabled;
    		$scope.formData.productName;
    		
    		$scope.gotoSelected = function() {
    			console.log($scope.url);
    			$state.go("dashboard.product." + $scope.url, {
    				'itemsPerPage': $scope.itemsPerPage, 
    				'currentPage': $scope.formData.currentPage,
    				'enabled': $scope.formData.enabled,
    				'productName': $scope.formData.productName
    			});
    		}
    	}
    })
    .state('dashboard.product.search',{
    	templateUrl:'views/product/search.html',
    	url:'/product/search',
    	params: {'itemsPerPage': 10, 'currentPage': 1, 'enabled': null, 'productName': null},
    	controller: 'SearchProductCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/searchProductCtrl.js']
              });
            },
            loadProducts:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/product/findProduct', {
            				enabled: $stateParams.enabled,
		        			currentPage: $stateParams.currentPage,
		        	    	itemsPerPage: $stateParams.itemsPerPage,
		        	    	productName: $stateParams.productName
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
    .state('dashboard.product.add',{
    	templateUrl:'views/product/add.html',
    	url:'/product/add',
    	params: {'data': null},
    	controller: 'AddProductCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/addProductCtrl.js']
              });
            }
    	}
    })
    .state('dashboard.product.databaseConf',{
    	templateUrl:'views/product/database_conf.html',
    	url:'/product/databaseConf',
    	params: {'data': null},
    	controller: 'DatabaseConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/databaseConfCtrl.js']
              });
            }
    	}
    })
    .state('dashboard.product.importPaymentConf',{
    	templateUrl:'views/product/import_payment_conf.html',
    	url:'/product/importPaymentConf',
    	params: {'id': null, 'productName': null},
    	controller: 'ImportPaymentConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/importPaymentConfCtrl.js', 'styles/product_import_conf.css']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/product/getColumnFormatPayment?id=' + $stateParams.id).then(function(data){
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
    .state('dashboard.product.importConf',{
    	templateUrl:'views/product/import_conf.html',
    	url:'/product/importConf',
    	params: {'id': null, 'productName': null},
    	controller: 'ImportConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:[
                         'scripts/controllers/product/importConfCtrl.js', 
                         'styles/product_import_conf.css'
                         ]
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/product/getColumnFormat?id=' + $stateParams.id).then(function(data){
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
    .state('dashboard.product.importConf.detailConf',{
    	templateUrl:'views/product/detail_conf.html',
    	url:'/detailConf',
    	params: {'productId': null},
    	controller: 'DetailConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/detailConfCtrl.js', 'styles/detailCof.css']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/product/getColumnFormatDet?productId=' + $stateParams.productId).then(function(data){
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
            }/*,
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/user/getProfile?username=' + $localStorage.username).then(function(data){
		            		if(data.data.statusCode != 9999) {
		            			$rootScope.systemAlert(data.data.statusCode);
		            			return $q.reject(data);
		            		}
            		
		            		return data.data;
		            	}, function(response) {
		            		$rootScope.systemAlert(response.status);
		        	    });
            }*/
    	}
    })
    //------------------------------------: New Task :-------------------------------------------
    .state('dashboard.newtask',{
    	templateUrl:'views/newtask/main.html',
    	url:'/newtask',
    	params: {'currentPage': 1, 'itemsPerPage': 10, productId: null},
    	controller: 'NewtaskCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/newtask/newtaskCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/newTask/findAll', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $stateParams.productId || $rootScope.workingOnProduct.id
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
    //------------------------------------: Import Others  :-------------------------------------------
    .state('dashboard.importOthers',{
    	templateUrl:'views/import_others/main.html',
    	url:'/importOthers',
    	params: {currentPage: 1, itemsPerPage: 10, productInfo: null, menuInfo: null},
    	controller: 'ImportOthersCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/import_others/importOthersCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/importOthers/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $stateParams.productInfo.id,
						menuId: $stateParams.menuInfo.id
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
    .state('dashboard.importOthers.search',{
    	templateUrl:'views/import_others/search.html',
    	url:'/search',
    	params: {currentPage: 1, itemsPerPage: 10, productInfo: null, menuInfo: null, fileId: null},
    	controller: 'ImportOthersSearchCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/import_others/importOthersSearchCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/importOthersDetail/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $stateParams.productInfo.id,
						menuId: $stateParams.menuInfo.id,
						fileId: $stateParams.fileId
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
    .state('dashboard.importOthersViewSetting',{
    	templateUrl:'views/import_others/import_others_conf.html',
    	url:'/importOthersViewSetting',
    	params: {productInfo: null, menuInfo: null},
    	controller: 'ImportOthersConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/import_others/importOthersConfCtrl.js', 'styles/product_import_conf.css']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/importMenu/getColumnFormat?productId=' + $stateParams.productInfo.id + '&menuId=' + $stateParams.menuInfo.id).then(function(data){
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
    .state('dashboard.importOthersViewSetting.detailsSetting',{
    	templateUrl:'views/import_others/import_others_detail_conf.html',
    	url:'/detailSetting',
    	params: {productInfo: null, menuInfo: null},
    	controller: 'ImportOthersDetailConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/import_others/importOthersDetailConfCtrl.js', 'styles/detailCof.css']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/importMenu/getColumnFormatDet?productId=' + $stateParams.productInfo.id + '&menuId=' + $stateParams.menuInfo.id).then(function(data){
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
    //------------------------------------: Task Detail :-------------------------------------------
    .state('dashboard.taskdetail',{
    	templateUrl:'views/taskdetail/main.html',
    	url:'/taskdetail',
    	params: {'currentPage': 1, 'itemsPerPage': 10, taskFileId: null, productId: null, fromPage: null},
    	controller: 'TaskDetailCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/taskdetail/taskdetailCtrl.js', 'styles/taskdetail_style.css']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/taskDetail/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						taskFileId: $stateParams.taskFileId,
						productId: $stateParams.productId,
						fromPage: $stateParams.fromPage
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
     //------------------------------------: Assigning task :-------------------------------------------
    .state('dashboard.assigntask',{
    	templateUrl:'views/assigntask/main.html',
    	url:'/assigntask',
    	params: {'currentPage': 1, 'itemsPerPage': 10, productId: null},
    	controller: 'AssignTaskCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/assigntask/assigntaskCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/newTask/findAll', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $stateParams.productId ||  $rootScope.workingOnProduct.id
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
    //------------------------------------: Working :-------------------------------------------
    .state('dashboard.working',{
    	templateUrl:'views/working/main.html',
    	controller: function($scope, $state) {
    		$scope.fromPage = 'working';
    		
    		$scope.gotoSelected = function() {
    			$state.go("dashboard.working." + $scope.url);
    		}
    	}
    })
    .state('dashboard.working.search',{
    	templateUrl:'views/working/search.html',
    	url:'/working/search',
    	params: {'currentPage': 1, 'itemsPerPage': 10, 'fromPage': 'working'},
    	controller: 'SearchWorkingCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/working/searchWorkingCtrl.js']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/taskDetail/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id,
						isActive: true,
						columnName: $stateParams.columnName,
						order: $stateParams.order,
						owner: $rootScope.group4 ? $rootScope.userId : null,
						fromPage: $stateParams.fromPage
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
    .state('dashboard.working.search.view',{
    	templateUrl:'views/working/view.html',
    	url:'/view',
    	params: {'id': null, traceCurrentPage: 1, traceItemsPerPage: 5, productId: null, currentPagePayment: 1, itemsPerPagePayment: 5},
    	controller: 'ViewWorkingCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:[
                         'scripts/controllers/working/viewWorkingCtrl.js',
                         'scripts/directives/datepicker/datepicker.js',
                         'styles/checkAnimate.css'
                         ]
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/taskDetail/view', {
            		id: $stateParams.id,
            		traceCurrentPage: $stateParams.traceCurrentPage,
            		traceItemsPerPage: $stateParams.traceItemsPerPage,
            		currentPagePayment: $stateParams.currentPagePayment,
            		itemsPerPagePayment: $stateParams.itemsPerPagePayment,
            		productId: $stateParams.productId,
            		isInit: true
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
    
    
    
    
    
    //------------------------------------: Template Of Payment :-------------------------------------------
    .state('dashboard.paymentTemplate',{
    	templateUrl:'views/payment_result_report/main.html',
    	url:'/paymentTemplate',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'PaymentResultReportCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment_result_report/paymentResultReportCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/paymentReport/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    
    
    
     //------------------------------------: Template Of Notice :-------------------------------------------
    .state('dashboard.noticeTemplate',{
    	templateUrl:'views/notice/main.html',
    	url:'/notice',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'NoticeUploadCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/notice/noticeUploadCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/notice/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
     //------------------------------------: Template Of Notice XDoc :-------------------------------------------
    .state('dashboard.noticeTemplateXDoc',{
    	templateUrl:'views/notice_xdoc/main.html',
    	url:'/noticeXDoc',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'NoticeXDocUploadCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/notice_xdoc/noticeXDocUploadCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/noticeXDoc/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    
    
    //------------------------------------: War File :-------------------------------------------
    .state('dashboard.warFile',{
    	templateUrl:'views/war_file/main.html',
    	url:'/warFile',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'WarFileCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/war_file/warFileCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/program/findAll', {
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
     //------------------------------------: Deployer File :-------------------------------------------
    .state('dashboard.deployerFile',{
    	templateUrl:'views/deployer_file/main.html',
    	url:'/deployerFile',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'DeployerFileCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/deployer_file/deployerFileCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/program/findAllDeployer', {
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
    
    //------------------------------------: Tunnel File :-------------------------------------------
    .state('dashboard.tunnelFile',{
    	templateUrl:'views/tunnel/main.html',
    	url:'/tunnelFile',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'TunnelFileCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/tunnel/tunnelFileCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/program/findAllTunnel', {
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
    
    //------------------------------------: Python File :-------------------------------------------
    .state('dashboard.pythonFile',{
    	templateUrl:'views/python/main.html',
    	url:'/pythonFile',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'PythonFileCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/python/pythonFileCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/program/findAllPython', {
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
    
    
    //------------------------------------: Payment :-------------------------------------------
    .state('dashboard.payment',{
        templateUrl:'views/payment/main.html'
    })
    .state('dashboard.payment.search',{
    	templateUrl:'views/payment/search.html',
    	url:'/payment',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'PaymentUploadCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment/paymentUploadCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/payment/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    .state('dashboard.payment.detail',{
    	templateUrl:'views/payment/detail.html',
    	url:'/paymentDetail',
    	params: {'currentPage': 1, 'itemsPerPage': 10, 'fileId': null, productId: null},
    	controller: 'PaymentDetailCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment/paymentDetailCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/paymentDetail/find', {
            			fileId: $stateParams.fileId,
						productId: $stateParams.productId,
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
    .state('dashboard.payment.detail2',{
    	templateUrl:'views/payment/detail.html',
    	url:'/paymentDetail2',
    	params: {'currentPage': 1, 'itemsPerPage': 10, isShowPage: true},
    	controller: 'PaymentDetailCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment/paymentDetailCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	var today = new Date($rootScope.serverDateTime);
            	var dateFrom = angular.copy(today);
            	var dateTo = angular.copy(today);
            	
            	dateFrom.setHours(0,0,0,0);
            	dateTo.setHours(23,59,59,999);
            	
            	return $http.post(urlPrefix + '/restAct/paymentDetail/find', {
						productId: $rootScope.workingOnProduct.id,
						currentPage: $stateParams.currentPage,
						itemsPerPage: $stateParams.itemsPerPage,
						owner: $rootScope.group4 ? $rootScope.userId : null,
						dateFrom: dateFrom,
						dateTo: dateTo
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
    //------------------------------------: Trace Result Import :-------------------------------------------
    .state('dashboard.traceResultImport',{
    	templateUrl:'views/trace_result_import/main.html',
    	url:'/traceResultImport',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'TraceResultImportCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/trace_result_import/traceResultImportCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/traceResultImport/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    //------------------------------------: Trace Result Report :-------------------------------------------
    .state('dashboard.traceResultResport',{
    	templateUrl:'views/trace_result_report/main.html',
    	url:'/traceResultResport',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'TraceResultReportCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/trace_result_report/traceResultReportCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/traceResultReport/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    
    //------------------------------------: Forecast Result Report :-------------------------------------------
    .state('dashboard.forecastResultResport',{
    	templateUrl:'views/forecast_result_report/main.html',
    	url:'/forecastResultResport',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'ForecastResultReportCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/forecast_result_report/forecastResultReportCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/forecastResultReport/find', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    
     //------------------------------------: Export Template :-------------------------------------------
    .state('dashboard.exportTemplate',{
    	templateUrl:'views/export_template/main.html',
    	url:'/exportTemplate',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'ExportTemplateCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/export_template/exportTemplateCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/newTask/findExportTemplate', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    //------------------------------------: Action Code Conf. :-------------------------------------------
    .state('dashboard.actionCodeConf',{
    	templateUrl:'views/action_code/main.html',
    	url:'/actionCode',
    	controller: 'ActionCodeCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/action_code/actionCodeCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/code/findActionCode', {
						productId: ($rootScope.setting && $rootScope.setting.currentProduct) ||  $rootScope.products[0].id,
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
    //------------------------------------: Result Code Conf. :-------------------------------------------
    .state('dashboard.resultCodeConf',{
    	templateUrl:'views/result_code/main.html',
    	url:'/resultCode',
    	controller: 'ResultCodeCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/result_code/resultCodeCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/code/findResultCode', {
            			productId: ($rootScope.setting && $rootScope.setting.currentProduct) ||  $rootScope.products[0].id,
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
    //------------------------------------: Trace Result :-------------------------------------------
    .state('dashboard.traceResult',{
        templateUrl:'views/trace_result/main.html',
        url:'/traceResult',
        params: {'currentPage': 1, 'itemsPerPage': 10, 'columnName': 'createdDateTime', 'order': 'desc', 'dateColumnName': 'createdDateTime'},
    	controller: "TraceResultCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/trace_result/traceResultCtrl.js',
                         'lib/bootstrap-submenu.min.js',
                         'lib/css/bootstrap-submenu.min.css']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	var today = new Date($rootScope.serverDateTime);
            	var dateFrom = angular.copy(today);
            	var dateTo = angular.copy(today);
            	
            	dateFrom.setHours(0,0,0,0);
            	dateTo.setHours(23,59,59,999);
            	
            	return $http.post(urlPrefix + '/restAct/traceWork/traceResult', {
					currentPage: $stateParams.currentPage, 
					itemsPerPage: $stateParams.itemsPerPage,
					columnName: $stateParams.columnName,
					dateColumnName: $stateParams.dateColumnName,
					dateFrom: dateFrom,
					dateTo: dateTo,
					order: $stateParams.order,
					productId: ($rootScope.setting && $rootScope.setting.currentProduct) ||  $rootScope.workingOnProduct.id,
					owner: $rootScope.group4 ? $rootScope.userId : null,
					isInit: true
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
    
    //------------------------------------: Forecast :-------------------------------------------
    .state('dashboard.forecast',{
        templateUrl:'views/forecast/main.html',
        url:'/forecast',
        params: {'currentPage': 1, 'itemsPerPage': 10, 'columnName': 'createdDateTime', 'order': 'desc', 'dateColumnName': 'createdDateTime'},
    	controller: "ForecastCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/forecast/forecastCtrl.js',
                         'lib/bootstrap-submenu.min.js',
                         'scripts/directives/datepicker/datepicker.js',
                         'lib/css/bootstrap-submenu.min.css']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	var today = new Date($rootScope.serverDateTime);
            	var dateFrom = angular.copy(today);
            	var dateTo = angular.copy(today);
            	
            	dateFrom.setHours(0,0,0,0);
            	dateTo.setHours(23,59,59,999);
            	
            	return $http.post(urlPrefix + '/restAct/forecast/forecastResult', {
					currentPage: $stateParams.currentPage, 
					itemsPerPage: $stateParams.itemsPerPage,
					columnName: $stateParams.columnName,
					dateColumnName: $stateParams.dateColumnName,
					dateFrom: dateFrom,
					dateTo: dateTo,
					order: $stateParams.order,
					productId: ($rootScope.setting && $rootScope.setting.currentProduct) ||  $rootScope.workingOnProduct.id,
					owner: $rootScope.group4 ? $rootScope.userId : null,
					isInit: true
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
    
    //------------------------------------: Manage Notice :-------------------------------------------
    .state('dashboard.manageNotice',{
        templateUrl:'views/manage_notice/main.html',
        url:'/manageNotice',
        params: {'currentPage': 1, 'itemsPerPage': 10, 'columnName': 'createdDateTime', 'order': 'desc'},
    	controller: "ManageNoticeCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/manage_notice/manageNoticeCtrl.js']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	var today = new Date($rootScope.serverDateTime);
            	var dateFrom = angular.copy(today);
            	var dateTo = angular.copy(today);
            	
            	dateFrom.setHours(0,0,0,0);
            	dateTo.setHours(23,59,59,999);
            	
            	return $http.post(urlPrefix + '/restAct/noticeManager/findToPrint', {
					currentPage: $stateParams.currentPage, 
					itemsPerPage: $stateParams.itemsPerPage,
					columnName: $stateParams.columnName,
					dateFrom: dateFrom,
					dateTo: dateTo,
					order: $stateParams.order,
					productId: $rootScope.workingOnProduct.id,
					owner: $rootScope.group4 ? $rootScope.userId : null,
					isInit: true,
					status: false
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
    
    //------------------------------------: DB Backup Show :-------------------------------------------
    .state('dashboard.dbBackupShow',{
        templateUrl:'views/db_backup_show/main.html',
        url:'/dbBackupShow',
    	controller: "DbBackupShowCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/db_backup_show/dbBackupShowCtrl.js']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/setting/findDBBackup', {
					isInit: true
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
    
    //------------------------------------: Tools :-------------------------------------------
    .state('dashboard.tools',{
        templateUrl:'views/tools/main.html',
        url:'/tools',
    	controller: function($scope, $state){
    		$scope.gotoSelected = function() {
    			$scope.isShowBack = false;
    			$scope.titlePanel = 'Tool Menu';
    			$state.go("dashboard.tools");
    		}
    	},
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/directives/tools_menu/menu.js']
              });
            }
    	}
    })
    .state('dashboard.tools.fileConvert',{
        templateUrl:'views/tools/fileConvert.html',
        params: {type: null, desc: null},
        url:'/fileConvert',
    	controller: 'FileConvertCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/tools/fileConvertCtrl.js']
              });
            }
    	}
    })
    
    //------------------------------------: Batch Notice :-------------------------------------------
    .state('dashboard.batchNotice',{
        templateUrl:'views/batch_notice/main.html',
        url:'/batchNotice',
        params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'BatchNoticeCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/batch_notice/batchNoticeCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/noticeXDoc/findBatchNotice', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
    
    //------------------------------------: Payment online checking :-------------------------------------------
    .state('dashboard.payOnlineChecking',{
        templateUrl:'views/pay_online_checking/main.html',
        url:'/payOnlineChecking',
        params: {'currentPage': 1, 'itemsPerPage': 10},
    	controller: 'PayOnlineCheckingCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/pay_online_checking/payOnlineCheckingCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/noticeXDoc/findBatchNotice', {
						currentPage: $stateParams.currentPage, 
						itemsPerPage: $stateParams.itemsPerPage,
						productId: $rootScope.workingOnProduct.id
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
     //------------------------------------: Home :-------------------------------------------
    .state('dashboard.home',{
        templateUrl:'views/home/main.html',
        url:'/home',
    	controller: "HomeCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/home/homeCtrl.js']
              });
            }
    	}
    })
    
     //------------------------------------: PaymentToUs :-------------------------------------------
    .state('dashboard.paymentToUs',{
        templateUrl:'views/payment_to_us/main.html',
        url:'/paymentToUs',
    	controller: "PaymentToUsCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment_to_us/paymentToUsCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, $localStorage, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/contact/findAccNo').then(function(data){
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
      //------------------------------------: ContactUs :-------------------------------------------
    .state('dashboard.contactUs',{
        templateUrl:'views/contact_us/main.html',
        url:'/contactUs',
    	controller: "ContactUsCtrl",
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/contact_us/contactUsCtrl.js']
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
}]);


//------------------------------------------------------------
app.run(['$rootScope', '$http', '$q', '$localStorage', '$state', '$window', 'toaster', 'urlPrefix', function ($rootScope, $http, $q, $localStorage, $state, $window, toaster, urlPrefix) {
	  console.log('Start app');
	  
	  $rootScope.state = $state;
	  var windowElement = angular.element($window);
	  windowElement.on('beforeunload', function (event) {
		// do whatever you want in here before the page unloads.        
		// the following line of code will prevent reload or navigating away.
		event.preventDefault();
	  });
	  
	  $rootScope.systemAlert = function(code, title, bodyMsg) {
			if(code == undefined) {
				alert('Unknown error! please contact admin');
			}else if(code == 0) {
				alert('Service Unavailable!  please contact admin');
				$window.location.href = urlPrefix + '/logout';
			}else if(code == 403) {
				alert('Access denied!  you are not authorized to access this service');
				$window.location.href = urlPrefix + '/logout';
			}else if(code == 401) {
				alert('Session expired! please login again');
				delete $localStorage.token;
				$window.location.href = urlPrefix + '/logout';
			}else if(code == 9999) {
				toaster.pop({
	                type: 'success',
	                title: title,
	                body: bodyMsg
	            });
			}else if(code == 'warn') {
				toaster.clear();
				toaster.pop({
	                type: 'warning',
	                title: title,
	                body: bodyMsg
	            });
			}else{
				toaster.clear();
				toaster.pop({
	                type: 'error',
	                title: title || 'Server service error('+code+')',
	                body: bodyMsg
	            });
			}
	  }
	  
	  //-----------------------------------------------------------------------------------
	  
	  
	  if($localStorage.token && Object.keys($localStorage.token)[0]) {
//	  if($localStorage.token) {
		  
		  //---------: Ignored the refreshToken process so just go to login page if have refresh page:
//		  $localStorage.token = null;
//		  $window.location.href = urlPrefix + '/logout';
//		  return;
		  //------------------------------------------------------------------------------------------
		  
		  $http.post(urlPrefix + '/refreshToken', {'token': $localStorage.token[Object.keys($localStorage.token)[0]]}).
		  then(function(data) {
			  
			  	var userData = data.data;
			  	$rootScope.isLicenseNotValid = userData.isLicenseNotValid; 
		    	
		    	if($rootScope.isLicenseNotValid) {
		    		$state.go("login");
		    		return
		    	}
		    	
		    	if(!$localStorage.token) {
		    		$localStorage.token = {};
		    	}
		    	
		    	//[Local Storage]
		    	$localStorage.token[userData.username] = userData.token;
		    	
		    	$rootScope.showname = userData.showname;
		    	$rootScope.username = userData.username;
		    	$rootScope.userId = userData.userId;
		    	$rootScope.setting = userData.setting;
		    	$rootScope.products = userData.products;
		    	$rootScope.workingOnProduct = $rootScope.products[0];
		    	$rootScope.showname = userData.showname;
		    	$rootScope.authority = userData.authorities[0].authority;
		    	$rootScope.serverDateTime = userData.serverDateTime;
		    	$rootScope.firstName = userData.firstName;
		    	$rootScope.lastName = userData.lastName;
		    	$rootScope.phoneNumber = userData.phoneNumber;
		    	$rootScope.phoneExt = userData.phoneExt;
		    	$rootScope.title = userData.title;
		    	$rootScope.companyName = userData.companyName;
		    	$rootScope.workingTime = userData.workingTime;
		    	$rootScope.backendVersion = userData.version;
		    	$rootScope.phoneWsServer = userData.phoneWsServer;
		    	$rootScope.phoneRealm = userData.phoneRealm;
		    	$rootScope.phonePass = userData.phonePass;
		    	$rootScope.isOutOfWorkingTime = userData.isOutOfWorkingTime;
		    	$rootScope.productKey = userData.productKey;
		    	
		    	if(userData.photo) {			
		    		$rootScope.photoSource = 'data:image/JPEG;base64,' + userData.photo;
		    	} else {
		    		$rootScope.photoSource = null;
		    	}
		    	
		    	$state.go("dashboard.home");
		  }, function(response) {
		    	console.log(response);
		    	$state.go("login");
		  });
	  }
	  
}])