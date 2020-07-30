angular.module('sbAdminApp').controller('LoginCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $localStorage, $base64, $log, toaster, urlPrefix) {
	
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
	        	$state.go("dashboard.home");
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
		    	
		        $scope.authenticated = true;
		        $scope.msg = null;
		        warningMsg = userData.warning;
		        
		        if(userData.photo) {			
		        	$rootScope.photoSource = 'data:image/JPEG;base64,' + userData.photo;
		    	} else {
		    		$rootScope.photoSource = null;
		    	}
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
	
	if($stateParams.action == 'logout') {
		$localStorage.token = {};
	}
	
});