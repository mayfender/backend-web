angular.module('sbAdminApp').controller('LoginCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $base64, $cookieStore, $localStorage, $log, toaster, urlPrefix) {
	
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
	    	$scope.isLicenseNotValid = userData.isLicenseNotValid;
	    	$scope.isDisabled = userData.isDisabled;
	    	
	    	if($scope.isLicenseNotValid || $scope.isDisabled) {
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
		    	$rootScope.workingOnProduct = $rootScope.products && $rootScope.products[0];
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
		    	$rootScope.webExtractIsEnabled = userData.webExtractIsEnabled;
		    	
		        $scope.authenticated = true;
		        $scope.msg = null;
		        
		        if(userData.photo) {			
		        	$rootScope.photoSource = 'data:image/JPEG;base64,' + userData.photo;
		    	} else {
		    		$rootScope.photoSource = null;
		    	}
		        
		        $rootScope.websocketService($rootScope.username);
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
		if(false) {
			$rootScope.saveRestTimeOut({action: 'end'}, function() {
				logout();
				delete $localStorage.token;
			});			
		} else {
			logout();
			delete $localStorage.token;
		}
	}
	
	var myModal;
	var isDismissModal;
	$scope.enterLicense = function() {
		$http.get(urlPrefix + '/restAct/setting/getData').then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
    			$rootScope.systemAlert(result.statusCode);
    			return;
    		}
			
			$scope.productKey = result.setting && result.setting.productKey;
			
			if(!myModal) {
				myModal = $('#enterLicenseModal').modal();			
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
				myModal.on('hidden.bs.modal', function (e) {
					//
				});
			} else {			
				myModal.modal('show');
			}
	    }, function(response) {
	    	
	    });
	}
	
	$scope.dismissModal = function() {
		if(!myModal) return;
		
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.updateLicense = function() {
		$http.post(urlPrefix + '/restAct/setting/updateLicense',{
			productKey: $scope.productKey,
			license: $scope.license
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
    			$rootScope.systemAlert(result.statusCode);
    			return;
    		}
			
			$scope.isLicenseNotValid = false;
	    }, function(response) {
	    	
	    });
		
		$scope.dismissModal();
		$scope.license = null;
	}
	
	$scope.contactUs = function() {
		$http.get(urlPrefix + '/restAct/setting/contactUs').then(function(data) {
			
			var result = data.data;
			if(result.statusCode != 9999) {
    			$rootScope.systemAlert(result.statusCode);
    			return;
    		}
	    }, function(response) {
	    	
	    });
	}
	
	function guid() {
//	    var uuidFormat = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
	    var uuidFormat = 'xxxxxxxxxx';
	    var nums = window.crypto.getRandomValues(new Uint8ClampedArray(uuidFormat.split(/[xy]/).length - 1));
	    var pointer =0;

	    return uuidFormat.replace(/[xy]/g, function(c) {  
	        var r = nums[pointer++] % 16,
	            v = (c === 'x') ? r : (r&0x3|0x8);

	        return v.toString(16);
	    });
	}
	
});