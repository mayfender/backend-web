angular.module('lineInfoApp', ['cp.ngConfirm']).controller('LineInfoCtrl', function($scope, $timeout, $q, $ngConfirm) {
	$scope.lineData = {};
	
	$scope.copy = function() {
		var $temp = $("<input>");
		$("body").append($temp);
		$temp.val($("#displayName").text() + ", " + $("#userId").text() + ", " + $("#getDecodedIDToken").text()).select();
		document.execCommand("copy");
		$temp.remove();
		$scope.copyStatus = 'ก๊อบปี๊แล้ว';
	}
	
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
