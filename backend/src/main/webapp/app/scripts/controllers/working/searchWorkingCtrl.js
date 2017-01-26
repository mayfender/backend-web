angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.headers = loadData.headers;
	$scope.headersPayment = loadData.headersPayment;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.$parent.headerTitle = 'แสดงข้อมูลงาน';
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	$scope.$parent.product = $rootScope.products[0];
	$scope.column = $stateParams.columnName;
	$scope.order = $stateParams.order;
	var taskDetailIds = loadData.taskDetailIds;
	var lastCol;
	
	$scope.searchBtn = function() {
		$scope.formData.currentPage = 1;
		$scope.search(false);
	}
	
	$scope.search = function(isNewLoad, searchIds) {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $scope.$parent.product.id,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			searchIds: searchIds
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			if(loadData.taskDetails) {				
				$scope.taskDetails = loadData.taskDetails;	
			} else {
				$scope.taskDetails = null;
			}
			
			$scope.totalItems = loadData.totalItems;
			
			if(isNewLoad) {		
				$scope.headers = loadData.headers;
				$scope.users = loadData.users;
			}
			
			if(!searchIds) {
				taskDetailIds = loadData.taskDetailIds;				
			}
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.currentPage = 1;
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.search(isNewLoad);
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
		$scope.formData.currentPage = 1;
		$scope.search(false);
	}
	
	$scope.view = function(data) {
		$scope.lastTaskView = data;
		$scope.idActive = data.id;
		
		$scope.isEditable = $rootScope.group4 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$state.go('dashboard.working.search.view', {id: data.id, productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $scope.$parent.product.id});
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $scope.$parent.product) return;
		
		$scope.column = 'sys_nextTimeDate';
		$scope.order = 'asc';
		$scope.formData.itemsPerPage = 10;
		$scope.$parent.product = prod;
		$scope.clearSearchForm(true);
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function() {
		var searchIdsObj = taskDetailIds.slice((($scope.formData.currentPage - 1) * $scope.formData.itemsPerPage), ($scope.formData.itemsPerPage * $scope.formData.currentPage));
		var searchIds = [];
		
		for(x in searchIdsObj) {
			searchIds.push(searchIdsObj[x].id);
		}
		
		$scope.search(false, searchIds);
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search(false);
	}
	//---------------------------------: Paging :----------------------------------------
	
});