angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd/MM/yyyy";
	var ownerColumn = $filter('filter')($scope.headers, {columnName: 'owner'});
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
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			columnSearchSelected: $scope.columnSearchSelected.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			
			clearState();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		if(lastCol) {	
			angular.element("i[id='" + lastCol + "_asc']").css('color', 'blue');
			angular.element("i[id='" + lastCol + "_desc']").css('color', 'blue');
		}
		
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.column = null;
		$scope.columnSearchSelected = $scope.columnSearchLst[0];
		$scope.search();
	}
	
	$scope.columnOrder = function(col) {
		$scope.column = col;
		
		if(lastCol) {
			angular.element("i[id='" + lastCol + "_asc']").css('color', 'blue');
			angular.element("i[id='" + lastCol + "_desc']").css('color', 'blue');
		}
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc') {			
			angular.element("i[id='" + $scope.column + "_asc']").css('color', 'red');
			angular.element("i[id='" + $scope.column + "_desc']").css('color', 'blue');
			$scope.order = 'asc';
		} else if($scope.order == 'asc' || $scope.order == null) {
			angular.element("i[id='" + $scope.column + "_asc']").css('color', 'blue');
			angular.element("i[id='" + $scope.column + "_desc']").css('color', 'red');			
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