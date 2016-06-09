angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd/MM/yyyy";
	var order;
	
	$scope.search = function(col, order) {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order
		}).then(function(data) {
			var data = data.data;
			
			if(data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.taskDetails = data.taskDetails;	
			$scope.totalItems = data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	var lastCol;
	$scope.columnOrder = function(col) {
		$scope.column = col;
		
		if(lastCol) {
			angular.element('#' + lastCol + '_desc').css('color', 'blue');
			angular.element('#' + lastCol + '_asc').css('color', 'blue');
		}
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc') {			
			angular.element('#' + col + '_asc').css('color', 'red');
			angular.element('#' + col + '_desc').css('color', 'blue');
			$scope.order = 'asc';
		} else if($scope.order == 'asc' || $scope.order == null) {
			angular.element('#' + col + '_asc').css('color', 'blue');
			angular.element('#' + col + '_desc').css('color', 'red');
			$scope.order = 'desc';
		}
		
		lastCol = $scope.column;
		$scope.search();
	}
	
	
	
	
	$scope.test = function() {
		console.log($scope.formData);
	}
	
	
	
	
	
	
	
	

	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});