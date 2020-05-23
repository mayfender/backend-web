angular.module('sbAdminApp').controller('LoginCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $cookieStore, $localStorage, $base64, $log, toaster, urlPrefix) {
	
	var windowElement = angular.element($window);
	windowElement.on('beforeunload', function (event) {
		// do whatever you want in here before the page unloads.        
		// the following line of code will prevent reload or navigating away.
		event.preventDefault();
	});
	
	$rootScope.systemAlert = function(code, title, bodyMsg) {
		if(code == undefined) alert('Unknown error! please contact admin');
		else if(code == 0) {
			alert('Service Unavailable!  please contact admin');
			$window.location.href = urlPrefix + '/logout';
		}else if(code == 403) {
			alert('Access denied!  you are not authorized to access this service');
			$window.location.href = urlPrefix + '/logout';
		}else if(code == 401) {
			alert('Seesion expired! please login again');
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
	
	
	$scope.login = function() {		
		authenticate($scope.credentials, function() {
	        if ($scope.authenticated) {
	        	$state.go("dashboard.dictionary");
	        } else {
	        	toaster.clear();
	        	toaster.pop({
	                type: 'error',
	                title: $scope.msg
	            });
	        }
	   });
	}
	
	var authenticate = function(credentials, callback) {
	    $http.post(urlPrefix + '/login', {'username': credentials.username,'password': $base64.encode(credentials.password)}).
	    then(function(data) {
	    	
	    	var userData = data.data;
	    	$scope.isDisabled = userData.isDisabled;
	    	
	    	if($scope.isDisabled) {
	    		return
	    	}
	    	
		    if (userData.token) {
		    	if(!$localStorage.token) {
		    		$localStorage.token = {};
		    	} else {
		    		if(Object.keys($localStorage.token)[0] == '0') {
		    			delete $localStorage.token;
		    			$localStorage.token = {};
		    		}
		    	}
		    	//[Local Storage]
		    	$localStorage.token[userData.username] = userData.token;
		    	if(!$localStorage.deviceId) {
		    		$localStorage.deviceId = guid();
		    	}
		    	
		    	$rootScope.showname = userData.showname;
		    	$rootScope.username = userData.username;
		    	$rootScope.userId = userData.userId;
		    	$rootScope.setting = userData.setting;
		    	$rootScope.products = userData.products;
		    	$rootScope.authority = userData.authorities[0].authority;
		    	
		    	if($rootScope.authority == 'ROLE_SUPERADMIN' || $rootScope.authority == 'ROLE_MANAGER') {
		    		$rootScope.products.unshift({id: null, productName:'--: Select Ports :--'});
		    	}
		    	
		    	$rootScope.workingOnProduct = $rootScope.products && $rootScope.products[0];
		    	$rootScope.showname = userData.showname;
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
		    	$rootScope.webExtractIsEnabled = userData.webExtractIsEnabled;
		    	
		        $scope.authenticated = true;
		        $scope.msg = null;
		        warningMsg = userData.warning;
		        
		        if(userData.photo) {			
		        	$rootScope.photoSource = 'data:image/JPEG;base64,' + userData.photo;
		    	} else {
		    		$rootScope.photoSource = null;
		    	}
		        
		        $rootScope.websocketService($rootScope.userId);
		    } else {
		    	$scope.authenticated = false;
		    	$scope.msg = 'Account does not exist';
		    }
		    
		    callback && callback();
	    }, function(response) {
	    	if(response.status == 401) {
	    		$scope.msg = 'Account does not exist';
	    	} else if(response.status == 410) {
	    		$scope.msg = 'นอกเวลาทำงาน';
	    	} else {
	    		$scope.msg = 'Failed to Connect';	    		
	    	}
	    	
	    	$scope.authenticated = false;
	    	callback && callback();
	    });
	}
	
	var logout = function() {
		$http.post(urlPrefix + '/logout', {}).
		then(function(data) {
			$scope.authenticated = false;
		}, function(response) {
			$scope.authenticated = false;
		});
	}
	
	if($stateParams.action == 'logout') {
		logout();
	}
	
});