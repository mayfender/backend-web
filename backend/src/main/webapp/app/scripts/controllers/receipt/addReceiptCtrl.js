angular.module('sbAdminApp').controller('AddReceiptCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, roles, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.$parent.headerTitle = 'ทำรายการ (' + $scope.serviceTypeText +')';
	$scope.persisBtn = "บันทึก";
	$scope.isEdit = false;
	$scope.criteria = {};
	
	
	$scope.save = function() {
		
		console.log($scope.criteria);
		
		$http.post(urlPrefix + '/restAct/serviceData/print', $scope.criteria).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
});