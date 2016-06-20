angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.receivUsers = angular.copy($scope.users);
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.noOwnerCount = loadData.noOwnerCount;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10, calColumn: loadData.balanceColumn, taskType: 1};
	$scope.format = "dd/MM/yyyy";
	$scope.assignMethods = [{id: 1, methodName: 'แบบสุ่ม'}, {id: 2, methodName: 'แบบดูประสิทธิภาพ'}];
	$scope.userMoreThanTask = false;
	$scope.numColumn = $filter('filter')($scope.headers, {dataType: 'num'});
	var ownerColumn = $filter('filter')($scope.headers, {columnName: 'owner'})[0];
	$scope.columnSearchLst = [{id: 1, colName: 'อื่นๆ'}];
	$scope.columnSearchSelected = $scope.columnSearchLst[0];
	var lastCol;
	
	if(ownerColumn) {
		$scope.columnSearchLst[1] = {id: 2, colName: ownerColumn.columnNameAlias || ownerColumn.columnName}
	}
	
	$scope.countSelected = 0;
	var lastRowSelected;
	var lastIndex;
	
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
	
	$scope.updateActive = function(obj) {
		var results = isActiveToggle(obj);
		
		$http.post(urlPrefix + '/restAct/taskDetail/updateTaskIsActive', {
			id: obj.id,
			isActives: results,
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
	
	var myModal;
	var isDismissModal;
	$scope.showCollector = function() {
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
	
	var myModal2;
	var isDismissModal2;
	$scope.showCollector2 = function() {
		
		if(!myModal2) {
			myModal2 = $('#myModal2').modal();			
			myModal2.on('hide.bs.modal', function (e) {
				if(!isDismissModal2) {
					return e.preventDefault();
				}
				isDismissModal2 = false;
			});
		} else {			
			myModal2.modal('show');
		}
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.dismissModal2 = function() {
		isDismissModal2 = true;
		myModal2.modal('hide');
	}
	
	$scope.selectAllUsersCheckBox = function() {
		if($scope.users.length > $scope.countSelected) {
			if($scope.isSelectAllUsers) {
				$scope.userMoreThanTask = true;
			} else {
				$scope.userMoreThanTask = false;
			}
			return;							
		} else {
			for (x in $scope.users) {
				$scope.users[x].isSelectUser = $scope.isSelectAllUsers;
			}			
		}
	}
	
	$scope.$watch('users', function(newVal, oldVal){
	    var isSelected = false;
	    var count = 0;
	    for (x in $scope.users) {
			if($scope.users[x].isSelectUser) {
				count++;
				
				if(count > $scope.countSelected) {
					$scope.userMoreThanTask = true;
					isSelected = false;
					break;
				} else {
					$scope.userMoreThanTask = false;
					isSelected = true;
				}
			}
		}
	    
	    if($scope.users.length == count) {
	    	$scope.isSelectAllUsers = true;
	    } else {
	    	$scope.isSelectAllUsers = false;
	    }
	    $scope.isOneSelected = isSelected;
	}, true);
	
	$scope.taskAssigningBySelected = function() {
		var selectedUsers = $filter('filter')($scope.users, {isSelectUser: true});
		var selectedTask = $filter('filter')($scope.taskDetails, {selected: true});
		var usernames = [];
		var taskIds = [];
		
		for (x in selectedUsers) {
			usernames.push({username: selectedUsers[x].username, showname: selectedUsers[x].showname});
		}		
		for (x in selectedTask) {
			taskIds.push(selectedTask[x].id);
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/taskAssigningBySelected', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			isActive: $scope.formData.isActive,
			columnSearchSelected: $scope.columnSearchSelected.id,
			usernames: usernames,
			methodId: $scope.formData.methodId,
			calColumn: $scope.formData.calColumn,
			taskIds: taskIds
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
			clearState();
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
		var result = [];
		
		for(i in obj) {
			if(obj[i].sys_isActive.status) {
				obj[i].sys_isActive.status = false;
				result.push({id: obj[i].id, status: obj[i].sys_isActive.status});
			} else {
				obj[i].sys_isActive.status = true;
				result.push({id: obj[i].id, status: obj[i].sys_isActive.status});
			}
		}
		
		return result;
	}
	
	function clearState() {
		lastRowSelected = null;
		lastIndex = null;
		$scope.countSelected = 0;
	}
	
	//-----------------------------------: Row selection :---------------------------------------
	$scope.rowSelect = function(data, index, e) {
		//--: right click
		if(e.which == 3) {
			return;
		}
		
		var isPressedCtrl = window.event.ctrlKey;
		var isPressedshift = window.event.shiftKey;
		
		if(isPressedCtrl) {
			lastRowSelected = data;
			lastIndex = index;
			
			if(data.selected) {
				data.selected = false;			
				$scope.countSelected--;
				if($scope.countSelected == 0) lastRowSelected = null;
			} else {
				data.selected = true;
				$scope.countSelected++;
			}
		} else if(isPressedshift && lastRowSelected) {
			if(lastIndex > index) {
				lastRowSelected = data;
				
				for (; index < lastIndex; index++) { 
					if($scope.taskDetails[index].selected) continue;
					
					$scope.taskDetails[index].selected = true;
					$scope.countSelected++;
				}
			} else if(lastIndex < index) {
				lastRowSelected = data;
				
				for (; lastIndex <= index; lastIndex++) { 
					if($scope.taskDetails[lastIndex].selected) continue;
					
					$scope.taskDetails[lastIndex].selected = true;
					$scope.countSelected++;
				}
			} else {				
				console.log('Nothing to do.');
			}
		}
	}	
	
	$scope.dndDragend = function() {
		var ids = [];
		for(x in $scope.receivUsers) {
			ids.push($scope.receivUsers[x].id);
		}
		
		$http.post(urlPrefix + '/restAct/user/reOrder', {
			ids: ids
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
	
	//-----------------------------------: Right click context menu :---------------------------------------
	
	$scope.contextMenuSelected = function(menu) {
		var selectedData = $filter('filter')($scope.taskDetails, {selected: true});
		
		if(selectedData.length == 0) {
			alert('กรุณาเลือกอย่างน้อย 1 รายการ');
			return;
		}
		
		switch(menu) {
		case 1: $scope.showCollector(); break;
		case 2: {
			$scope.updateActive(selectedData); 
			break;
		}
		}
	}
	
	
});