angular.module('sbAdminApp').controller('PaymentDetailCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.paymentDetails = loadData.paymentDetails;
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;	
	
	$scope.$parent.isDetailPage = !$stateParams.isShowPage;
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่บันทึก'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}
	                          ];
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	$scope.formData.dateFrom.setHours(0,0,0,0);
	$scope.formData.dateTo.setHours(23,59,59,999);

	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/paymentDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			fileId: $stateParams.fileId,
			productId: $stateParams.productId,
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$scope.paymentDetails = loadData.paymentDetails;
			$scope.totalItems = loadData.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.$parent.gotoSelected = function() {
		$state.go("dashboard.payment.search");
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.$on("$destroy", function() {
		$scope.$parent.isDetailPage = false
    });
	    	
});