angular.module('sbAdminApp').controller('ContactUsCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	console.log('Contact Us');
	$scope.formData = {};
	
	$scope.sent = function() {
		$http.post(urlPrefix + '/restAct/contact/sentMail', {
			name: $scope.formData.name, 
			mobile: $scope.formData.mobile, 
			line: $scope.formData.line, 
			email: $scope.formData.email, 
			detail: $scope.formData.detail
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			$rootScope.systemAlert(data.data.statusCode, 'Send');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}

});