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
    'dateParser'
  ])
  
  .run(function(editableOptions) {
	  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
  })
  
  .value('urlPrefix', '/backend') //-------- '/ricoh' or ''
  
  .value('roles', [{authority:'ROLE_ADMIN', name:'Admin'},
                   {authority:'ROLE_MANAGER', name:'Manager'},
                   {authority:'ROLE_SUPERADMIN', name:'Superadmin'},
                   {authority:'ROLE_SUPERVISOR', name:'Supervisor'},
                   {authority:'ROLE_USER', name:'User'}])
                   
  .value('roles2', [{authority:'ROLE_SUPERVISOR', name:'Supervisor'},{authority:'ROLE_USER', name:'User'}])
  
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider', '$httpProvider', '$translateProvider',
           function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, $httpProvider, $translateProvider) {
	 
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
     /* .state('dashboard.home',{
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
      })*/
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
    //------------------------------------: User :-------------------------------------------
    .state('dashboard.user',{
        templateUrl:'views/user/main.html',
    	controller: function($scope, $state, loadProducts){
    		$scope.itemsPerPage = 10;
    		$scope.formData = {currentPage : 1};
    		$scope.formData.enabled;
    		$scope.formData.role;
    		$scope.formData.userName;
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
		        	    	currentProduct: $localStorage.setting && $localStorage.setting.currentProduct,
		        	    	product: $stateParams.product
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
    //------------------------------------: Employer :-------------------------------------------
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
    .state('dashboard.product.importConf',{
    	templateUrl:'views/product/import_conf.html',
    	url:'/product/importConf',
    	params: {'id': null, 'productName': null},
    	controller: 'ImportConfCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/product/importConfCtrl.js', 'styles/product_import_conf.css']
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
            },
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
            }
    	}
    })
    //------------------------------------: New Task :-------------------------------------------
    .state('dashboard.newtask',{
    	templateUrl:'views/newtask/main.html',
    	url:'/newtask',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
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
						currentProduct: $localStorage.setting && $localStorage.setting.currentProduct,
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
    	controller: 'ImportOthersViewSettingConfCtrl',
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
    	params: {'currentPage': 1, 'itemsPerPage': 10, taskFileId: null, productId: null},
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
     //------------------------------------: Assigning task :-------------------------------------------
    .state('dashboard.assigntask',{
    	templateUrl:'views/assigntask/main.html',
    	url:'/assigntask',
    	params: {'currentPage': 1, 'itemsPerPage': 10},
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
						currentProduct: $localStorage.setting && $localStorage.setting.currentProduct,
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
    //------------------------------------: Working :-------------------------------------------
    .state('dashboard.working',{
    	templateUrl:'views/working/main.html',
    	controller: function($scope) {
    		$scope.fromPage = 'working';
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
						productId: $localStorage.setting.currentProduct,
						isActive: true,
						owner: $localStorage.username,
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
    	params: {'id': null, traceCurrentPage: 1, traceItemsPerPage: 5},
    	controller: 'ViewWorkingCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/working/viewWorkingCtrl.js']
              });
            },
            loadData:function($rootScope, $localStorage, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.post(urlPrefix + '/restAct/taskDetail/view', {
            		id: $stateParams.id,
            		traceCurrentPage: $stateParams.traceCurrentPage,
            		traceItemsPerPage: $stateParams.traceItemsPerPage,
            		productId: $localStorage.setting.currentProduct,	
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
						productId: $localStorage.setting && $localStorage.setting.currentProduct,
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
						productId: ($localStorage.products[0] && $localStorage.products[0].id) || ($localStorage.setting && $localStorage.setting.currentProduct)
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
            			productId: ($localStorage.products[0] && $localStorage.products[0].id) || ($localStorage.setting && $localStorage.setting.currentProduct)
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
				alert('Seesion expired! please login again');
				delete $localStorage.token;
				$window.location.href = urlPrefix + '/logout';
			}else if(code == 9999) {
				toaster.pop({
	                type: 'success',
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
	  
	  if($localStorage.token) {
		  $http.post(urlPrefix + '/refreshToken', {'token': $localStorage.token}).
		  then(function(data) {
			  
			  	var userData = data.data;
			  	
		    	$localStorage.token = userData.token;
		    	$localStorage.showname = userData.showname;
		    	$localStorage.username = userData.username;
		    	$localStorage.authorities = userData.authorities;
		    	$localStorage.products = userData.products;
		    	$localStorage.setting = userData.setting;
		    	
		    	$rootScope.showname = userData.showname;
		    	$rootScope.authority = userData.authorities[0].authority;
		    	$rootScope.serverDateTime = userData.serverDateTime;
		    	
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