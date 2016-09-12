angular.module('sbAdminApp').controller('PaymentDetailCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix) {
	
	console.log('PaymentDetailCtrl');
	
	$scope.$parent.isDetailPage = true;
	
	$scope.$parent.gotoSelected = function() {
		$state.go("dashboard.payment.search");
	}
	
	
	
	/*console.log(loadData);
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd-MM-yyyy HH:mm:ss";
	var uploader;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/payment/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct)
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.files;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}*/
	
	$scope.$on("$destroy", function() {
		$scope.$parent.isDetailPage = false
    });
	    	
});