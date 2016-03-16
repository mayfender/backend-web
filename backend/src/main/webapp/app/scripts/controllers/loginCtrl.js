angular.module('sbAdminApp').controller('LoginCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $base64, $cookies, $log, toaster, urlPrefix) {
	
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
	    var headers = credentials ? {authorization : "Basic " + $base64.encode(credentials.username + ":" + credentials.password)} : {};

	    $http.get(urlPrefix + '/user', {headers : headers}).
	    then(function(data) {
	    	
	    	var principal = data.data.principal;
	    	var map = data.data.map;
	    	
		    if (principal.name) {
		    	$rootScope.principal = principal.principal;
		    	$rootScope.principal.userNameShow = map.userNameShow;
		    	$rootScope.principal.masterNamings = map.masterNamings;
		    	
		    	//-- Store in the cookies
		    	$cookies['userNameShow'] = $rootScope.principal.userNameShow;
		    	$cookies['username'] = $rootScope.principal.username;
		    	$cookies['authority'] = $rootScope.principal.authorities[0].authority;
		    	
		        $scope.authenticated = true;
		        $scope.msg = null;
		    } else {
		    	$scope.authenticated = false;
		    	$scope.msg = 'Account does not exist';
		    }
		    
		    callback && callback();
	    }, function(response) {
	    	if(response.status == 401) {
	    		$scope.msg = 'Account does not exist';
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