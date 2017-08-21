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
	$scope.taskDetailIds = loadData.taskDetailIds;
	
	if($scope.taskDetailIds && $scope.taskDetailIds.length > 0) {
		$scope.firstTaskDetailId = $scope.taskDetailIds[0].id;
		$scope.lastTaskDetailId = $scope.taskDetailIds[$scope.taskDetailIds.length - 1].id;		
	}
	
	$scope.dateColumnNames = [
	                          {col: 'sys_traceDate', text:'วันที่ติดตาม'}, 
	                          {col: 'sys_appointDate', text:'วันนัดชำระ'}, 
	                          {col: 'sys_nextTimeDate', text:'วันนัด Call'}
	                          ];
	var lastCol;
	
	$scope.searchBtn = function(from) {
		$scope.formData.currentPage = 1;
		if(from == 'detail') {			
			$scope.search(false, null, function() {
				/*if($scope.taskDetails.length == 0) {
					$rootScope.systemAlert('warn', 'ไม่พบข้อมูล');
				} else {
					$scope.$$childHead.firstTask();					
				}*/
				$scope.$$childHead.firstTask();					
			});
		} else {			
			$scope.search(false);
		}
	}
	
	$scope.search = function(isNewLoad, searchIds, callback) {		
		
		if($scope.formData.dateTo) {
			$scope.formData.dateTo.setHours(23,59,59);			
		}
		if($scope.formData.dateFrom) {
			$scope.formData.dateFrom.setHours(0,0,0);			
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			searchIds: searchIds,
			isPgs: $scope.formData.isPgs,
			isNoTrace: $scope.formData.isNoTrace,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo
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
				$scope.headersPayment = loadData.headersPayment; 
			}
			
			if(!searchIds) {
				$scope.taskDetailIds = loadData.taskDetailIds;	
				if($scope.taskDetailIds && $scope.taskDetailIds.length > 0) {
					$scope.firstTaskDetailId = $scope.taskDetailIds[0].id;
					$scope.lastTaskDetailId = $scope.taskDetailIds[$scope.taskDetailIds.length - 1].id;
				}
			}
			
			callback && callback();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.goToItem = function(keyEvent) {
		if (keyEvent.which !== 13 || !$scope.formData.itemToGo) return;
		
		if($scope.formData.itemToGo > $scope.totalItems) {
			$scope.formData.itemToGo = null;
			return;
		}
		
		var item = $scope.taskDetailIds[$scope.formData.itemToGo - 1];
		var page = parseInt(($scope.formData.itemToGo - 1) / $scope.formData.itemsPerPage);
		$scope.formData.currentPage = (page + 1);
		$scope.pageChanged(function(){
			var task;
			
			for(var i in $scope.taskDetails) {
				task = $scope.taskDetails[i]
				
				if(task.id == item.id) {
					$scope.view(task);
					break;
				}
			}
			$scope.formData.itemToGo = null;
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad, from) {
		$scope.formData.currentPage = 1;
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.dateColumnName = null;
		$scope.formData.dateFrom = null;
		$scope.formData.dateTo = null;
		
		if(from == 'detail') {
			$scope.searchBtn(from);
		} else {			
			$scope.search(isNewLoad);
		}
	}
	
	$scope.columnOrder = function(col, from) {
		$scope.column = col;
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc' || $scope.order == null) {			
			$scope.order = 'asc';
		} else if($scope.order == 'asc') {
			$scope.order = 'desc';
		}
		
		lastCol = $scope.column;
		$scope.searchBtn(from);
	}
	
	$scope.view = function(data) {
		$scope.lastTaskView = data;
		$scope.idActive = data.id;
		
		$scope.getCurrentIndex();
		
		$scope.isEditable = $rootScope.group6 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$state.go('dashboard.working.search.view', {id: data.id, productId: $rootScope.workingOnProduct.id});
	}
	
	$scope.getCurrentIndex = function() {
		for(var i in $scope.taskDetailIds) {
			if($scope.taskDetailIds[i].id == $scope.idActive) {
				$scope.currentIndex = i;
				break;
			}
		}
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $rootScope.workingOnProduct) return;
		
		$scope.column = 'sys_nextTimeDate';
		$scope.order = 'asc';
		$scope.formData.itemsPerPage = 10;
		$rootScope.workingOnProduct = prod;
		$scope.clearSearchForm(true);
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function(callback) {
		var searchIdsObj = $scope.taskDetailIds.slice((($scope.formData.currentPage - 1) * $scope.formData.itemsPerPage), ($scope.formData.itemsPerPage * $scope.formData.currentPage));
		var searchIds = [];
		
		for(x in searchIdsObj) {
			searchIds.push(searchIdsObj[x].id);
		}
		
		$scope.search(false, searchIds, callback);
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search(false);
	}
	//---------------------------------: Paging :----------------------------------------
	
	
	$scope.dateColumnNameChanged = function() {
		$scope.formData.dateColumnName || ($scope.formData.dateFrom = null); ($scope.formData.dateTo = null);
	}
});