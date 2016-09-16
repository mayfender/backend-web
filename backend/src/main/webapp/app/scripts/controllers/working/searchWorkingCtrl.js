angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.formData = {currentPage : 1, itemsPerPage: 5};
	$scope.headers = loadData.headers;
	$scope.headersPayment = loadData.headersPayment;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails.slice(0, $scope.formData.itemsPerPage);	
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.$parent.headerTitle = 'แสดงข้อมูลงาน';
	$scope.formData.owner = $rootScope.group4 ? $localStorage.username : null;
	$scope.$parent.product = $rootScope.products[0];
	var lastCol;
	
	$scope.search = function() {
		$scope.formData.currentPage = 1;
		
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $scope.$parent.product.id,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			if(loadData.taskDetails) {				
				$scope.taskDetails = loadData.taskDetails.slice(0, $scope.formData.itemsPerPage);	
			} else {
				$scope.taskDetails = null;
			}
			
			$scope.totalItems = loadData.totalItems;
			$scope.headers = loadData.headers;
			$scope.users = loadData.users;
			
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
		$scope.lastTaskView = data;
		$scope.idActive = data.id;
		$scope.isEditable = $rootScope.group4 ? (data.sys_owner[0].username == $localStorage.username) : true;
		$state.go('dashboard.working.search.view', {id: data.id, productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $scope.$parent.product.id});
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $scope.$parent.product) return;
		
		$scope.formData.itemsPerPage = 5;
		$scope.$parent.product = prod;
		$scope.clearSearchForm();
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function() {
		$scope.taskDetails = loadData.taskDetails.slice((($scope.formData.currentPage - 1) * $scope.formData.itemsPerPage), ($scope.formData.itemsPerPage * $scope.formData.currentPage));
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.taskDetails = loadData.taskDetails.slice(0, $scope.formData.itemsPerPage);
	}
	//---------------------------------: Paging :----------------------------------------
	
});