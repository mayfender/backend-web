angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.userTaskCount = loadData.userTaskCount;
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.usersSearch = angular.copy(loadData.users);
	$scope.usersSearch.splice(0, 0, {id: '-1', username: '', showname: '--งานว่าง--'});
	
	$scope.transferUsers = angular.copy($scope.users);
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.noOwnerCount = loadData.noOwnerCount;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10, taskType: 1, owner: null};
	$scope.assignMethods = [{id: 1, methodName: 'แบบสุ่ม'}, {id: 2, methodName: 'แบบดูประสิทธิภาพ'}];
	$scope.userMoreThanTask = false;
	$scope.countSelected = 0;
	
	$scope.selectedItems = [];	
	$scope.headersSelectedItem = $filter('filter')($scope.headers, {columnName: loadData.contractNoColumn});
	
	$scope.dateColumnNames = $filter('filter')($scope.headers, {dataType: 'date'});
	if($scope.dateColumnNames.length == 1) {
		$scope.formData.dateColumnName = $scope.dateColumnNames[0].columnName;
	}
	
	var lastCol;
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
			owner: $scope.formData.owner,
			isActive: $scope.formData.isActive,
			fromPage: $stateParams.fromPage,
			dateColumnName: $scope.formData.dateColumnName,
			dateValue: $scope.formData.date
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
		$scope.formData.owner = null;
		$scope.formData.date = null
		$scope.column = null;
		$scope.search();
	}
	
	$scope.gotoSelected = function() {
		if($stateParams.fromPage == 'assign') {
			$state.go("dashboard.assigntask", {productId: $stateParams.productId});			
		} else if($stateParams.fromPage == 'upload') {
			$state.go("dashboard.newtask", {productId: $stateParams.productId});
		}
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
		$scope.search();
	}
	
	$scope.dateColumnNameChanged = function() {
		$scope.formData.dateColumnName || ($scope.formData.date = null);
	}
	
	var myModal;
	var isDismissModal;
	$scope.showCollector = function() {
		$scope.userMoreThanTask = false;
		$scope.countSelectedDummy = angular.copy($scope.countSelected);
		for (x in $scope.users) {
			$scope.users[x].isSelectUser = false;
		}
		
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
		$scope.formData.taskType = 1;
		$scope.userMoreThanTask = false;
		$scope.countSelectedDummy = angular.copy($scope.noOwnerCount);
		console.log($scope.countSelectedDummy);
		
		for (x in $scope.users) {
			$scope.users[x].isSelectUser = false;
			$scope.transferUsers[x].isSelectUser = false;
		}
			
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
	
	var myModal3;
	var isDismissModal3;
	$scope.showSelectedList = function() {
		if(!myModal3) {
			myModal3 = $('#myModal3').modal();			
			myModal3.on('hide.bs.modal', function (e) {
				if(!isDismissModal3) {
					return e.preventDefault();
				}
				isDismissModal3 = false;
			});
		} else {			
			myModal3.modal('show');
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
	
	$scope.dismissModal3 = function() {
		isDismissModal3 = true;
		myModal3.modal('hide');
	}
	
	$scope.selectAllUsersCheckBox = function() {
		if($scope.users.length > $scope.countSelectedDummy) {
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
		console.log($scope.countSelectedDummy);
	    
	    var count = checkUserSelected();
	    
	    if($scope.users.length == count) {
	    	$scope.isSelectAllUsers = true;
	    } else {
	    	$scope.isSelectAllUsers = false;
	    }
	}, true);
	
	$scope.$watch('transferUsers', function(newVal, oldVal){
		if($scope.formData.taskType == 1) return;
		
		var dummy;
		var dummy2;
		var count = 0;
		
		for (x in $scope.transferUsers) {
			dummy = $scope.transferUsers[x];
			if(dummy.isSelectUser) {
				count += $scope.userTaskCount[dummy.username];
			}
			
			for (y in $scope.users) {
				dummy2 = $scope.users[y];
				if(dummy2.username == dummy.username) {
					if(dummy.isSelectUser) {
						dummy2.isDisabled = true;
					} else {
						dummy2.isDisabled = false;						
					}
				}
			}
		}
		
		
		$scope.countSelectedDummy = count;
		checkUserSelected();		
		
	}, true);
	
	function checkUserSelected() {
		var count = 0;
		var isSelected = false;
		
		for (x in $scope.users) {
			if($scope.users[x].isSelectUser) {
				count++;
				
				if(count > $scope.countSelectedDummy) {
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
		return count;
	}
	
	$scope.taskAssigningBySelected = function() {
		var selectedUsers = $filter('filter')($scope.users, {isSelectUser: true});
		var selectedTask = $filter('filter')($scope.taskDetails, {selected: true});
		var usernames = [];
		var taskIds = [];
		
		for (x in selectedUsers) {
			usernames.push({id: selectedUsers[x].id, username: selectedUsers[x].username, showname: selectedUsers[x].showname});
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
			usernames: usernames,
			methodId: $scope.formData.methodId,
			taskIds: taskIds,
			fromPage: $stateParams.fromPage
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			$scope.noOwnerCount = result.noOwnerCount;
			$scope.userTaskCount = result.userTaskCount;
			
			$scope.dismissModal();
			clearState();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.taskAssigningWhole = function() {
		var usernames = [];
		var transferUsernames = [];
		var selectedUsers = $filter('filter')($scope.users, {isSelectUser: true});
		var selectedTransferUsers = $filter('filter')($scope.transferUsers, {isSelectUser: true});
		
		for (x in selectedUsers) {
			usernames.push({id: selectedUsers[x].id, username: selectedUsers[x].username, showname: selectedUsers[x].showname});
		}
				
		for (x in selectedTransferUsers) {
			transferUsernames.push(selectedTransferUsers[x].id);
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/taskAssigningWhole', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			isActive: $scope.formData.isActive,
			usernames: usernames,
			transferUsernames: transferUsernames,
			methodId: $scope.formData.methodId,
			taskType: $scope.formData.taskType,
			fromPage: $stateParams.fromPage
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
			$scope.noOwnerCount = result.noOwnerCount;
			$scope.userTaskCount = result.userTaskCount;
			
			$scope.dismissModal2();
			clearState();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.taskTypeChange = function() {
		if($scope.formData.taskType == 1) {
			$scope.countSelectedDummy = angular.copy($scope.noOwnerCount);
		} else {
			$scope.countSelectedDummy = $filter('filter')($scope.transferUsers, {isSelectUser: true}).length;			
		}
		
		for (x in $scope.users) {
			$scope.users[x].isSelectUser = false;
			$scope.users[x].isDisabled = false;
			$scope.transferUsers[x].isSelectUser = false;
		}
		$scope.userMoreThanTask = false;
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
//		$scope.countSelectedDummy = 0;
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
		case 3: {
			break;
		}
		case 4: {
			addToList(selectedData);
			break;
		}
		case 5: {
			break;
		}
		}
	}
	
	function addToList(selectedData) {
		
		outer: for (y in selectedData) {
			for (x in $scope.selectedItems) {
				if($scope.selectedItems[x].id == selectedData[y].id) {
					continue outer;
				}
			}
			$scope.selectedItems.push(selectedData[y]);			
		}
	}
	
	$scope.clearSelectedList = function() {
		$scope.selectedItems = [];
	}
	
	$scope.deleteSelectedList = function(id) {
		for (x in $scope.selectedItems) {
			if($scope.selectedItems[x].id == id) {
				$scope.selectedItems.splice(x, 1);
				break;
			}
		}
	}
	
	var uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/taskDetail/uploadAssing', 
        headers:{'X-Auth-Token': $localStorage.token}, 
        formData: [{productId: $stateParams.productId, taskFileId: $stateParams.taskFileId || ''}]
    });
	
	// FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 10;
        }
    });

    // CALLBACKS
    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        console.log(fileItem);
        fileItem.upload();
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถนำเข้าได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        
        if(response.statusCode == 9999) {
        	$scope.formData.currentPage = 1;
        	$scope.formData.itemsPerPage = 10;
        	
        	$rootScope.systemAlert(response.statusCode, 'Work Assinging is completed.');
        	
        	$scope.search();        	
        } else {
        	$rootScope.systemAlert(response.statusCode);
        }
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	
    
    $('.dateSearch').datepicker({
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
    });
	
});