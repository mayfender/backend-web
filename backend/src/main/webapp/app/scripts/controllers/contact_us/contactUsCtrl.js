angular.module('sbAdminApp').controller('ContactUsCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix, FileUploader) {
	console.log('Contact Us');
	$scope.formData = {};
	
	$scope.sent = function() {		
		if($scope.uploader.queue.length > 0) {
			for(var x in $scope.uploader.queue) {
				$scope.uploader.queue[x].formData[0].name = $scope.formData.name || '';
				$scope.uploader.queue[x].formData[0].mobile = $scope.formData.mobile || '';
				$scope.uploader.queue[x].formData[0].line = $scope.formData.line || '';
				$scope.uploader.queue[x].formData[0].email = $scope.formData.email || '';
				$scope.uploader.queue[x].formData[0].detail = $scope.formData.detail || '';
			}
			$scope.isProcessing = true;
			$scope.uploader.uploadAll();
		} else {
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
	}
	
	$scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/contact/sentMailAttach', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{name: '', mobile: '', line: '', email: '', detail: ''}],
        queueLimit: 1
	});
	
	$scope.uploader.onCompleteItem = function(fileItem, response, status, headers) {
		$scope.isProcessing = false;
		
        if(response.statusCode != 9999) {
        	$rootScope.systemAlert(response.statusCode);
        	return;
        }
        
        $scope.uploader.clearQueue();
        $rootScope.systemAlert(response.statusCode, 'Send');
    };

});