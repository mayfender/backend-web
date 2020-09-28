angular.module('lineInfoApp', ['cp.ngConfirm', 'base64', 'ngStorage']).controller('LineInfoCtrl', function($rootScope, $scope, $timeout, $q, $ngConfirm, $http, $localStorage, $base64) {
	$scope.lineData = {};
	$scope.authenticated = true;
	
	$scope.copy = function() {
		var $temp = $("<input>");
		$("body").append($temp);
		$temp.val($("#displayName").text() + ", " + $("#userId").text() + ", " + $("#getDecodedIDToken").text()).select();
		document.execCommand("copy");
		$temp.remove();
		$scope.copyStatus = 'ก๊อบปี๊แล้ว';
	}
	
	$scope.sendBack = function() {
		if (!liff.isInClient()) {
	        alert('Mobile only.');
	    } else {
	        liff.sendMessages([
	        	{
	      		  type: 'image',
	      		  originalContentUrl: $scope.lineData.pictureUrl,
	      		  previewImageUrl: $scope.lineData.pictureUrl
	      		},
	      		{
	            'type': 'text',
	            'text': 'Display Name: '+ $scope.lineData.displayName + '\nUser ID: ' + $scope.lineData.userId
	      		}
	      	]).then(function() {
	            liff.closeWindow();
	        }).catch(function(error) {
	            window.alert('Error sending message: ' + error);
	        });
	    }
	}
	
	
	
	
	
	
	
	
	
	//-----------------------------------------------------------------
	function login(lineUserId) {		
		authenticate(lineUserId, function() {
	        if ($scope.authenticated) {
	        	window.location.href = './main.html?uid=' + lineUserId;
	        	console.log('Login success.');	        	
	        } else {
	        	if($scope.userNotFoundErr) {
	        		console.log('Not found user.');	        		
	        	} else {
	        		console.log('System Error.');	        			        		
	        	}
	        	$('#lps-overlay').css("display","none");	        
	        }
	   });
	}
	
	var authenticate = function(lineUserId, callback) {
	    $http.post('/backend/loginByLineUserId', {'lineUserId': $base64.encode(lineUserId)}).
	    then(function(data) {
	    	
	    	var userData = data.data;
	    	$scope.userNotFoundErr = userData.userNotFoundErr;
	    	console.log($scope.userNotFoundErr);
	    	
	    	if($scope.userNotFoundErr) {
	    		$scope.authenticated = false;
	    	} else {
			    if (userData.token) {
			        $scope.authenticated = true;
			    } else {
			    	$scope.authenticated = false;
			    }
	    	}
		    callback && callback();
	    }, function(response) {
	    	$scope.authenticated = false;
	    	callback && callback();
	    });
	}
	//-----------------------------------------------------------------
	
	function runApp() {
		return $q(function(resolve, reject) {
			liff.getProfile().then(profile => {
				resolve(profile);
			}).catch(err => reject(err));
		});
    }
	
	$scope.$watch('$viewContentLoaded', 
		function() {
			$timeout(function() {
			    liff.init({ liffId: "1654799308-eLAWR62j" }, () => {
			    	if (liff.isLoggedIn()) {
			    		runApp().then(function(profile) {
							$scope.lineData.pictureUrl = profile.pictureUrl;
							$scope.lineData.userId = profile.userId;
							$scope.lineData.displayName = profile.displayName;
							$scope.lineData.statusMessage = profile.statusMessage;
							$scope.lineData.getDecodedIDToken = liff.getDecodedIDToken().email;
							
							if(profile.userId) {
								login(profile.userId);
							} else {
								window.location.href = 'https://www.notfound.com';
							}
			        	}, function(err) {
			        		console.error(err)
			        	});
			      	} else {
			        	liff.login();
			     	}
			    }, err => console.error(err.code, error.message));
		},0);
	});
});
