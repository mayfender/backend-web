angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd-MM-yyyy";
	var ownerColumn = $filter('filter')($scope.headers, {columnName: 'sys_owner'});
	$scope.columnSearchLst = [{id: 1, colName: 'อื่นๆ'}];
	$scope.columnSearchSelected = $scope.columnSearchLst[0];
	var lastCol;
	
	if(ownerColumn) {
		$scope.columnSearchLst[1] = {id: 2, colName: ownerColumn[0].columnNameAlias || ownerColumn[0].columnName}
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $localStorage.setting && $localStorage.setting.currentProduct,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			columnSearchSelected: $scope.columnSearchSelected.id,
			owner: $localStorage.username
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log(result);
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.column = null;
		$scope.columnSearchSelected = $scope.columnSearchLst[0];
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
	
	$scope.searchColumnEvent = function(id) {
		if($scope.columnSearchSelected.id == id) return;
		
		$scope.formData.keyword = null;
		$scope.columnSearchSelected = $filter('filter')($scope.columnSearchLst, {id: id})[0];
	}
	
	$scope.view = function(id) {
		$scope.idActive = id;
		$state.go('dashboard.working.search.view', {id: id});
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