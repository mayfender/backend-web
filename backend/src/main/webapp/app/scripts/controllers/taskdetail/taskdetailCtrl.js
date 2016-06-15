angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.noOwnerCount = loadData.noOwnerCount;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10, calColumn: loadData.balanceColumn};
	$scope.format = "dd/MM/yyyy";
	$scope.assignMethods = [{id: 1, methodName: 'แบบสุ่ม'}, {id: 2, methodName: 'แบบดูประสิทธิภาพ'}];
	$scope.userMoreThanTask = false;
	$scope.numColumn = $filter('filter')($scope.headers, {dataType: 'num'});
	var ownerColumn = $filter('filter')($scope.headers, {columnName: 'owner'})[0];
	$scope.columnSearchLst = [{id: 1, colName: 'อื่นๆ'}];
	$scope.columnSearchSelected = $scope.columnSearchLst[0];
	
	if(ownerColumn) {
		$scope.columnSearchLst[1] = {id: 2, colName: ownerColumn.columnNameAlias || ownerColumn.columnName}
	}
	
	var lastRowSelected;
	var lastIndex;
	var countSelected = 0;
	var contextMenuSelectedData;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			isActive: $scope.formData.isActive,
			columnSearchSelected: $scope.columnSearchSelected.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			
			lastRowSelected = null;
			lastIndex = null;
			countSelected = 0;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.isActive = null;
		$scope.formData.keyword = null;
		$scope.columnSearchSelected = $scope.columnSearchLst[0];
		$scope.search();
	}
	
	$scope.isActiveClick = function(obj) {
		isActiveToggle(obj);
		
		$http.post(urlPrefix + '/restAct/taskDetail/updateTaskIsActive', {
			id: obj.id,
			isActive: obj.sys_isActive.status,
			productId: $stateParams.productId,
			taskFileId: $stateParams.taskFileId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				isActiveToggle(obj);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.noOwnerCount = result.noOwnerCount;
		}, function(response) {
			isActiveToggle(obj);	
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
	
	var myModal;
	var isDismissModal;
	$scope.showCollector = function() {
		if($scope.users.length > $scope.noOwnerCount) {
			$scope.isSelectAllUsers = false;
		} else {
			$scope.isSelectAllUsers = true;
		}
		
		$scope.selectAllUsersCheckBox();
		$scope.userMoreThanTask = false;
		
		if(!myModal) {
			myModal = $('#myModal').modal();			
			myModal.on('hide.bs.modal', function (e) {
				if(!isDismissModal) {
					return e.preventDefault();
				}
				isDismissModal = false;
			});
		} else {			
			myModal.modal('show');
		}
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.selectAllUsersCheckBox = function() {
		for (x in $scope.users) {
			$scope.users[x].isSelectUser = $scope.isSelectAllUsers;
		}
	}
	
	$scope.$watch('users', function(newVal, oldVal){
	    var isSelected = false;
	    var count = 0;
	    for (x in $scope.users) {
			if($scope.users[x].isSelectUser) {
				count++;
				
				if(count > $scope.noOwnerCount) {
					$scope.userMoreThanTask = true;
					isSelected = false;
					break;
				} else {
					$scope.userMoreThanTask = false;
					isSelected = true;
				}
			}
		}
	    
	    $scope.isOneSelected = isSelected;
	}, true);
	
	$scope.proceedAssigning = function() {
		var selectedUsers = $filter('filter')($scope.users, {isSelectUser: true});
		var usernames = [];
		
		for (x in selectedUsers) {
			usernames.push(selectedUsers[x].username);
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/taskAssigning', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order,
			usernames: usernames,
			methodId: $scope.formData.methodId,
			calColumn: $scope.formData.calColumn
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			$scope.noOwnerCount = result.noOwnerCount;
			
			$scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changeCalColumnEvent = function() {
		if($scope.formData.calColumn == null) return;
		
		$http.post(urlPrefix + '/restAct/product/updateBalanceColumn', {
			productId: $stateParams.productId,
			balanceColumn: $scope.formData.calColumn
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	$scope.searchColumnEvent = function(id) {
		if($scope.columnSearchSelected.id == id) return;
		
		$scope.formData.keyword = null;
		$scope.columnSearchSelected = $filter('filter')($scope.columnSearchLst, {id: id})[0];
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	function isActiveToggle(obj) {
		if(obj.sys_isActive.status) {
			obj.sys_isActive.status = false;
		} else {
			obj.sys_isActive.status = true;
		}
	}
	
	//-----------------------------------: Row selection :---------------------------------------
	$scope.rowSelect = function(data, index, e) {
		var isPressedCtrl = window.event.ctrlKey;
		var isPressedshift = window.event.shiftKey;
		
		//--: right click
		if(e.which == 3) {
			rightClick(data);
		} 
		
		if(isPressedCtrl) {
			lastRowSelected = data;
			lastIndex = index;
			
			if(data.selected) {
				data.selected = false;			
				countSelected--;
				if(countSelected == 0) lastRowSelected = null;
			} else {
				data.selected = true;
				countSelected++;
			}
		} else if(isPressedshift && lastRowSelected) {
			if(lastIndex > index) {
				lastRowSelected = data;
				
				for (; index < lastIndex; index++) { 
					if($scope.taskDetails[index].selected) continue;
					
					$scope.taskDetails[index].selected = true;
					countSelected++;
				}
			} else if(lastIndex < index) {
				lastRowSelected = data;
				
				for (; lastIndex < index; lastIndex++) { 
					if($scope.taskDetails[index].selected) continue;
					
					$scope.taskDetails[lastIndex + 1].selected = true;
					countSelected++;
				}
			} else {				
				console.log('Nothing to do.');
			}
		}
	}	
	
	//-----------------------------------: Right click context menu :---------------------------------------
	function rightClick(data) {
		contextMenuSelectedData = data;		
	}
	
	$scope.contextMenuSelected = function(menu) {
		switch(menu) {
		case 1: $scope.showCollector(); break;
		case 2: $scope.isActiveClick(contextMenuSelectedData); break;
		}
	}
	
	
});