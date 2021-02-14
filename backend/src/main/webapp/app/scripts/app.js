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
    'naif.base64',
    'toaster',
    'pascalprecht.translate',
    'ngStomp',
    'ngStorage',
    'cp.ngConfirm',
    'dateParser',
    'dndLists',
    'ngTagsInput',
    'xeditable'
  ])
  
  .run(function(editableOptions) {
	  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
  })
  
  .value('urlPrefix', '/backend') //-------- '/ricoh' or ''
  
  .value('roles', [
	  			   {authority:'ROLE_SUPERVISOR', name:'Supervisor'},
	  			   {authority:'ROLE_ADMIN', name:'Admin'}
	  			  ])
                   
   .value('roles2', [{authority:'ROLE_SUPERADMIN', name:'Superadmin'}])
   .value('roles3', [{authority:'ROLE_ADMIN', name:'Admin'}, {authority:'ROLE_AGENT', name:'Agent'}])
   .value('roles4', [])
   .value('roles5', [])
  
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
      .state('dashboard.main',{
        url:'/main',
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
      .state('dashboard.home',{
	        templateUrl:'views/home/main.html',
	        url:'/home',
	        controller: 'HomeCtrl',
	    	resolve: {
	            loadMyFiles:function($ocLazyLoad) {
	              return $ocLazyLoad.load({
	            	  name:'sbAdminApp',
	                  files:['scripts/controllers/home/homeCtrl.js']
	              });
	            }
	    	}
	    })
      .state('dashboard.order',{
        templateUrl:'views/order/main.html',
        url:'/order',
        controller: 'OrderCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/order/orderCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	if($rootScope.userId) {
            		return $http.get(urlPrefix + '/restAct/order/getPeriod?dealerId=' + $rootScope.workingOnDealer.id + '&isGetUsers=true').then(function(data){
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
    .state('dashboard.payment',{
        templateUrl:'views/payment/main.html',
        url:'/payment',
        controller: 'PaymentCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/payment/paymentCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	return $http.get(urlPrefix + '/restAct/order/getPeriod?dealerId=' + $rootScope.workingOnDealer.id + '&isGetUsers=true').then(function(data){
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
    .state('dashboard.lottoResult',{
        templateUrl:'views/lottoResult/main.html',
        url:'/lottoResult',
        controller: 'LottoResultCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/lottoResult/lottoResultCtrl.js']
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
    .state('dashboard.receiver',{
        templateUrl:'views/receiver/main.html',
        url:'/receiver',
        controller: 'ReceiverCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/receiver/receiverCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        		return $http.get(urlPrefix + '/restAct/receiver/getReceiverList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
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
    .state('dashboard.priceList',{
        templateUrl:'views/priceList/main.html',
        url:'/priceList',
        controller: 'PriceListCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/priceList/priceListCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        		return $http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
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
    .state('dashboard.sendRound',{
        templateUrl:'views/sendRound/main.html',
        url:'/sendRound',
        controller: 'SendRoundCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/sendRound/sendRoundCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        		return $http.get(urlPrefix + '/restAct/sendRound/getDataList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
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
    
    
    
    .state('dashboard.manageOrder',{
        templateUrl:'views/manageOrder/main.html',
        url:'/manageOrder',
        controller: 'ManageOrderCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/manageOrder/manageOrderCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        		return $http.get(urlPrefix + '/restAct/order/getPeriod?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
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
    .state('dashboard.dealer',{
        templateUrl:'views/dealer/main.html',
        url:'/dealer',
        controller: 'DealerCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/dealer/dealerCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        		return $http.get(urlPrefix + '/restAct/dealer/getDealerAll').then(function(data){
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
    	params: {'itemsPerPage': 10, 'currentPage': 1, 'enabled': null, 'role': null, 'userName': null},
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
		        	    	dealerId: $rootScope.workingOnDealer.id
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
    
  //---: For DMS Office  
	.state('dashboard.dmsCustomer',{
	    templateUrl:'views/dms/customer/main.html',
		controller: function($scope, $state){
			$scope.main = {};
			$scope.main.packages = [{id: 1, name: 'เช่า'}, {id: 2, name: 'ซื้อขาด'}];
			$scope.gotoSelected = function() {
				if($scope.main.page == 1) {
					$state.go("dashboard.dmsCustomer.add");
				} else if($scope.main.page == 2) {
					window.history.back();
				}
			}
		}
	})
  .state('dashboard.dmsCustomer.search',{
	templateUrl:'views/dms/customer/search.html',
	url:'/dmsCustomer/search',
	controller: 'SearchCtrl',
	resolve: {
        loadMyFiles:function($ocLazyLoad) {
          return $ocLazyLoad.load({
        	  name:'sbAdminApp',
              files:['scripts/controllers/dms/customer/searchCtrl.js']
          });
        },
        loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
        	return $http.post(urlPrefix + '/restAct/dms/getCustomers', {
	        			currentPage: 1,
	        	    	itemsPerPage: 10
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
    .state('dashboard.dmsCustomer.add',{
    	templateUrl:'views/dms/customer/add.html',
    	url:'/dmsCustomer/add',
    	params: {'data': null},
    	controller: 'AddCtrl',
    	resolve: {
            loadMyFiles:function($ocLazyLoad) {
              return $ocLazyLoad.load({
            	  name:'sbAdminApp',
                  files:['scripts/controllers/dms/customer/addCtrl.js']
              });
            },
            loadData:function($rootScope, $stateParams, $http, $state, $filter, $q, urlPrefix) {
            	if($stateParams.data) {
            		return $http.get(urlPrefix + '/restAct/dms/editCustomer?id=' + $stateParams.data._id).then(function(data){
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
    //---: For DMS Office
    
    
    
      
      
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
app.run(['$rootScope', '$http', '$q', '$localStorage', '$timeout', '$state', '$window', '$ngConfirm', '$translate', 'toaster', 'urlPrefix', function ($rootScope, $http, $q, $localStorage, $timeout, $state, $window, $ngConfirm, $translate, toaster, urlPrefix) {
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
		  $http.post(urlPrefix + '/refreshToken', {'token': $localStorage.token[Object.keys($localStorage.token)[0]]}).
		  then(function(data) {
			  
			  	var userData = data.data;
			  	
		    	if(!$localStorage.token) {
		    		$localStorage.token = {};
		    	}
		    	
		    	//[Local Storage]
		    	$localStorage.token[userData.username] = userData.token;
		    	
		    	$rootScope.showname = userData.showname;
		    	$rootScope.username = userData.username;
		    	$rootScope.userId = userData.userId;
		    	$rootScope.dealers = userData.dealers;
		    	$rootScope.authority = userData.authorities[0].authority;
		    	
		    	if($rootScope.authority == 'ROLE_SUPERADMIN') {
		    		$rootScope.dealers.unshift({id: null, name:'--: Select Dealer :--'});
		    	}
		    	$rootScope.workingOnDealer = $rootScope.dealers && $rootScope.dealers[0];
		    			    	
		    	$rootScope.serverDateTime = userData.serverDateTime;
		    	$rootScope.firstName = userData.firstName;
		    	$rootScope.lastName = userData.lastName;
		    	$rootScope.title = userData.title;
		    	$rootScope.companyName = userData.companyName;
		    	$rootScope.backendVersion = userData.version;
		    	
		    	if(userData.photo) {			
		    		$rootScope.photoSource = 'data:image/JPEG;base64,' + userData.photo;
		    	} else {
		    		$rootScope.photoSource = null;
		    	}
		    	
		    	$state.go("dashboard.home");
//		    	$state.go("dashboard.dealer");
		  }, function(response) {
		    	console.log(response);
		    	$state.go("login");
		  });
	  } else {
		  $state.go("login");
		  return;
	  }
	

}])



