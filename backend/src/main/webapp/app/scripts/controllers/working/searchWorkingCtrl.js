angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd-MM-yyyy";
	$scope.$parent.headerTitle = 'แสดงข้อมูลงาน';
	$scope.formData.owner = $rootScope.group4 ? $localStorage.username : null;
	$scope.$parent.product = $rootScope.products[0];
	var lastCol;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.group4 ? ($localStorage.setting && $localStorage.setting.currentProduct) : $scope.$parent.product.id,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			$scope.headers = result.headers;
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.column = null;
		$scope.formData.owner = $rootScope.group4 ? $localStorage.username : null;
		$scope.search();
	}
	
	$scope.columnOrder = function(col) {
		$scope.column = col;
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc') {			
			$scope.order = 'asc';
		} else if($scope.order == 'asc' || $scope.order == null) {
			$scope.order = 'desc';
		}
		
		lastCol = $scope.column;
		$scope.search();
	}
	
	$scope.view = function(data) {
		$scope.idActive = data.id;
		$scope.isEditable = $rootScope.group4 ? (data.sys_owner[0].username == $localStorage.username) : true;
		$state.go('dashboard.working.search.view', {id: data.id, productId: $rootScope.group4 ? ($localStorage.setting && $localStorage.setting.currentProduct) : $scope.$parent.product.id});
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $scope.$parent.product) return;
		
		$scope.$parent.product = prod;
		$scope.search();
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	//---------------------------------: Paging :----------------------------------------
	
});