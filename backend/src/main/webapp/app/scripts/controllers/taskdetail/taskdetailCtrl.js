angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, $ngConfirm, FileUploader, urlPrefix, loadData) {
	
	$scope.userTaskCount = loadData.userTaskCount;
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.dymList = loadData.dymList;
	$scope.dymSearch = loadData.dymSearch;
	$scope.usersSearch = angular.copy(loadData.users);
	$scope.usersSearch.splice(0, 0, {id: '-1', username: '', showname: '--งานว่าง--'});
	
	$scope.transferUsers = angular.copy($scope.users);
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.noOwnerCount = loadData.noOwnerCount;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10, taskType: 1, owner: null};
	$scope.formData.isActive = $rootScope.group6 ? true : null;
	$scope.formData.owner = $rootScope.group6 ? $rootScope.userId : null;
	
	$scope.assignMethods = [{id: 1, methodName: 'แบบสุ่ม'}, {id: 2, methodName: 'แบบดูประสิทธิภาพ'}];
	$scope.userMoreThanTask = false;
	$scope.countSelected = 0;
	
	$scope.selectedItems = [];	
	$scope.headersSelectedItem = $filter('filter')($scope.headers, {columnName: loadData.contractNoColumn});
	
	$scope.dateColumnNames = $filter('filter')($scope.headers, {dataType: 'date'});
	/*if($scope.dateColumnNames.length == 1) {
		$scope.formData.dateColumnName = $scope.dateColumnNames[0].columnName;
	}*/
	
	var lastCol;
	var lastRowSelected;
	var lastIndex;
	var itemFile;
	initGroup();
	
	function getSearchParams() {
		if($scope.formData.dateTo) {
			$scope.formData.dateTo.setHours(23,59,59);			
		}
		
		return {
				currentPage: $scope.formData.currentPage, 
				itemsPerPage: $scope.formData.itemsPerPage,
				taskFileId: $stateParams.taskFileId,
				productId: $rootScope.workingOnProduct.id,
				columnName: $scope.column,
				order: $scope.order,
				keyword: $scope.formData.keyword,
				tag: $scope.formData.tag,
				isNoTrace: $scope.formData.isNoTrace,
				owner: $scope.formData.owner,
				isActive: $scope.formData.isActive,
				fromPage: $stateParams.fromPage,
				dateColumnName: $scope.formData.dateColumnName,
				dateFrom: $scope.formData.dateFrom,
				dateTo: $scope.formData.dateTo,
				codeName: $scope.formData.codeName,
				codeValue: $scope.formData.codeValue,
				dymSearchFiedName: $scope.formData.dymSearchFieldName && $scope.formData.dymSearchFieldName.fieldName,
				dymSearchFiedVal: $scope.formData.dymSearchValue
			}
	}
	
	$scope.search = function(type) {
		if(type == 'remove' || type == 'enable' || type == 'disable') {
			var msg = type == 'remove' ? 'ยืนยันการลบข้อมูล' : type == 'enable' ? 'ยืนยันการ Enable' : 'ยืนยันการ Disable';
			var isConfirm = confirm(msg);
		    if(!isConfirm) return;
		}
		
		var params = getSearchParams();
		params.actionType = type;
		
		$http.post(urlPrefix + '/restAct/taskDetail/find', params).then(function(data) {
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
	
	var templateNameModal;
	var isTemplateNameDismissModal;
	$scope.exportTemplate = function(passParam) {
		$http.post(urlPrefix + '/restAct/newTask/findExportTemplate', {
			productId: $rootScope.workingOnProduct.id,
			enabled: 1,
			currentPage: 1,
			itemsPerPage: 100
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log(result);
			
			if(result.files && result.files.length == 1) {
				var params;
				
				if(!passParam) {
					params = getSearchParams();			
				} else {
					params = passParam;
				}
				params.fileId = result.files[0].id;
				$scope.exportByCriteria(params);
				return;
			}
			
			$scope.templateExportFiles = result.files;
			
			if(!templateNameModal) {
				templateNameModal = $('#templateNameModal').modal();			
				templateNameModal.on('shown.bs.modal', function (e) {
					//
				});
				templateNameModal.on('hide.bs.modal', function (e) {
					if(!isTemplateNameDismissModal) {
						return e.preventDefault();
					}
					isTemplateNameDismissModal = false;
				});
				templateNameModal.on('hidden.bs.modal', function (e) {
					//
  				});
			} else {			
				templateNameModal.modal('show');
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.templateNamedismissModal = function() {
		if(!templateNameModal) return;
		
		isTemplateNameDismissModal = true;
		templateNameModal.modal('hide');
	}
	
	$scope.exportByCriteriaButton = function(id) {
		var params = getSearchParams();			
		params.fileId = id;
		$scope.exportByCriteria(params);
		$scope.templateNamedismissModal();
	}
	
	$scope.exportByCriteria = function(passParam) {
		var params;
		
		if(!passParam) {
			params = getSearchParams();			
		} else {
			params = passParam;
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/exportByCriteria', params, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		if(lastCol) {	
			angular.element("i[id='" + lastCol + "_asc']").css('color', 'blue');
			angular.element("i[id='" + lastCol + "_desc']").css('color', 'blue');
		}
		
		if($rootScope.group6) {			
			$scope.formData.isActive = $rootScope.group6 ? true : null;
			$scope.formData.owner = $rootScope.group6 ? $rootScope.userId : null;
		} else {
			$scope.formData.isActive = null;
			$scope.formData.owner = null;			
		}
		
		$scope.formData.keyword = null;
		$scope.formData.tag = null;
		$scope.formData.dateFrom = null
		$scope.formData.dateTo = null
		$scope.column = null;
		$scope.formData.codeName = null;
		$scope.formData.codeValue = null;
		$scope.codeNameChange();
		$scope.formData.dymSearchFieldName = null;
		$scope.formData.dymSearchValue = null;
		
		
		$scope.search();
	}
	
	$scope.gotoSelected = function() {
		if($stateParams.fromPage == 'assign') {
			$state.go("dashboard.assigntask", {productId: $rootScope.workingOnProduct.id});			
		} else if($stateParams.fromPage == 'upload') {
			$state.go("dashboard.newtask", {productId: $rootScope.workingOnProduct.id});
		}
	}
	
	$scope.updateActive = function(obj) {
		var results = isActiveToggle(obj);
		
		$http.post(urlPrefix + '/restAct/taskDetail/updateTaskIsActive', {
			id: obj.id,
			isActives: results,
			productId: $rootScope.workingOnProduct.id,
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
		if(!$scope.formData.dateColumnName) {
			$scope.formData.dateFrom = null;
			$scope.formData.dateTo = null;
		}
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
			productId: $rootScope.workingOnProduct.id,
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
			productId: $rootScope.workingOnProduct.id,
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
		case 1: {
			$scope.showCollector(); 
			break;
		}
		case 2: {
			$scope.updateActive(selectedData); 
			break;
		}
		case 3: {
			removeTask(selectedData);
			break;
		}
		case 4: {
			addToList(selectedData);
			break;
		}
		case 5: {
			exportTask(selectedData);
			break;
		}
		}
	}
	
	function exportTask(selectedData) {
		var taskIds = [];
		
		for (x in selectedData) {
			taskIds.push(selectedData[x].id);
		}
		
		var params = getSearchParams();
		params.searchIds = taskIds;
		
		$scope.exportTemplate(params);
//		$scope.exportByCriteria(params);
	}
	
	function removeTask(selectedData) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    var taskIds = [];
		
		for (x in selectedData) {
			taskIds.push(selectedData[x].id);
		}
	    
	    var params = getSearchParams();
	    params.taskIds = taskIds;
		params.actionType = 'remove';
		
		$http.post(urlPrefix + '/restAct/taskDetail/taskUpdateByIds', params).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.taskDetails = result.taskDetails;	
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	$scope.selectedListAcion = function(type) {
		var taskIds = [];
		
		for (x in $scope.selectedItems) {
			taskIds.push($scope.selectedItems[x].id);
		}
		
		if(type == 'export') {
			var params = getSearchParams();	
			params.searchIds = taskIds;
			
			$scope.exportTemplate(params);
//			$scope.exportByCriteria(params);
		} else {
			var params = getSearchParams();
			params.taskIds = taskIds;
			
			if(type == 'enable') {
				params.actionType = 'enable';
			} else if(type == 'disable') {
				params.actionType = 'disable';
			} else if(type == 'remove') {
				var deleteUser = confirm('ยืนยันการลบข้อมูล');
			    if(!deleteUser) return;
				params.actionType = 'remove';
			}
			
			$http.post(urlPrefix + '/restAct/taskDetail/taskUpdateByIds', params).then(function(data) {
				var result = data.data;
				
				if(result.statusCode != 9999) {
					$rootScope.systemAlert(result.statusCode);
					return;
				}
				
				if(type == 'remove') {
					$scope.selectedItems = [];
				}
				
				$scope.taskDetails = result.taskDetails;	
				$scope.totalItems = result.totalItems;
				clearState();
				$scope.dismissModal3();
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
		}
	}
	
	$scope.updateTag = function(dataIn) {
		$http.post(urlPrefix + '/restAct/taskDetail/updateTags', {
			id: dataIn.id, 
			productId: $rootScope.workingOnProduct.id,
			tags: dataIn['sys_tags']
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
	
	
	var confirmObj, updatedMsg;
	var uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/taskDetail/uploadUpdate', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{productId: $rootScope.workingOnProduct.id, taskFileId: $stateParams.taskFileId || ''}]
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
        itemFile = fileItem;
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
    	$scope.statusMsg = 'กำลังดำเนินการ กรุณารอ...';
    	confirmObj = $ngConfirm({
    		title: 'รายงานการ Update ข้อมูล',
    		icon: 'fa fa-spinner fa-spin',
    		closeIcon: false,
    		type: 'orange',
    		scope: $scope,
    		content: '<strong>{{statusMsg}}</strong>',
    		buttons: {
    			OK: {
    				disabled: true,
    				text: '...',
    				btnClass: 'btn-orange',
    				action: function() {
    					$('#assign').val('');
    				}
    			} 
    		}
    	});
    	
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
        	
        	if(response.colDateTypes || response.colNotFounds) {
        		confirmObj.close();
        		$('#assign').val('');
        		
        		$scope.tabs = [{index: 0, title: 'คอลัมน์ วันที่', active: false}, {index: 1, title: 'คอลัมน์ ที่ไม่มีในระบบ', active: false}]
        		
        		if(response.colDateTypes.length > 0 || response.colNotFounds.length > 0) {        		
	        		$scope.colDateTypes = response.colDateTypes;
	        		$scope.colNotFounds = response.colNotFounds;
	        		$scope.importChk($scope.colDateTypes);
	        		
	        		if($scope.colNotFounds.length > 0) {
	        			$scope.tabs[1].active = true;
	        		}
	        	}
        	} else {
        		if(response.commonMsg) {
        			updatedMsg = response.commonMsg;
        		} else {
	        		if(response.updatedNo && response.updatedNo > 0) {
	        			$scope.search();
	        			updatedMsg = "มีการ Update ข้อมูล จำนวน " + response.updatedNo + " รายการ";
	        			confirmObj.setType('green');
	        			confirmObj.buttons.OK.setBtnClass('btn-green');
	        		} else {
	        			updatedMsg = "ไม่มีการ Update ข้อมูล";     
	        		}
        		}
        		$scope.statusMsg = updatedMsg;
        		confirmObj.setIcon('fa fa-info-circle');
        		confirmObj.buttons.OK.setDisabled(false);
        		confirmObj.buttons.OK.setText('OK');
        	}
        } else {
        	$rootScope.systemAlert(response.statusCode);
        	$('#assign').val('');
        }
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
    

    
    
    
    $('.input-daterange input').each(function() {
	    $(this).datepicker({
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'}
	    );
	});
    
    
  //-----------------------------------------------------------------
	var importChkModal;
	var isDismissImportChkModal;
	$scope.importChk = function() {		
		if(!importChkModal) {
			importChkModal = $('#importChkModal').modal();			
			importChkModal.on('hide.bs.modal', function (e) {
				if(!isDismissImportChkModal) {
					return e.preventDefault();
				}
				isDismissImportChkModal = false;
			});
			importChkModal.on('hidden.bs.modal', function (e) {
				//--
			});
		} else {			
			importChkModal.modal('show');
		}		
	}
	
	$scope.dismissImportChkModal = function(isRemove) {
		console.log(importChkModal);
		isDismissImportChkModal = true;
		importChkModal.modal('hide');
		
		if(isRemove) itemFile.remove();
	}
	
	$scope.proceedImport = function() {
		itemFile.formData[0].isConfirmImport = true;
		var yearTypes = new Array();
		var obj;
		
		for(var i in  $scope.colDateTypes) {
			obj = $scope.colDateTypes[i];
			yearTypes.push({columnName: obj.columnName, yearType: obj.yearType});
		}
		
		itemFile.formData[0].yearTypes = angular.toJson(yearTypes);
		
		itemFile.upload();
		$scope.dismissImportChkModal();
	}
	
	$scope.uploadItem = function(item) {
		console.log('uploadItem');
		itemFile = item;
		itemFile.upload();
	}
	
	//---------------------------------: Dynamic List :----------------------------------------
	$scope.codeNameChange = function() {
		$scope.selectedCodeName = $filter('filter')($scope.dymList, {fieldName: $scope.formData.codeName})[0];
		
		if(!$scope.selectedCodeName) {
			$scope.codeGroups = null;
			return;
		}
		
		if($scope.selectedCodeName.dymListDetGroup) {
			$scope.codeGroups = $scope.selectedCodeName.dymListDetGroup;
		} else {
			$scope.codeGroups = null;
		}
	}
	
	$scope.changeGroup = function(gp) {
		$scope.selectedCodeName.groupSelected = gp;
		$scope.selectedCodeName.dymListDet = $filter('filter')($scope.selectedCodeName.dymListDetDummy || $scope.selectedCodeName.dymListDet, {groupId: gp['_id']});
	}
	
	function initGroup() {
		var list;
		
		for(i in $scope.dymList) {
			list = $scope.dymList[i];
			list.groupSelected = list.dymListDetGroup[0];
			
			if(list.groupSelected) {				
				list.dymListDetDummy = list.dymListDet;
				list.dymListDet = $filter('filter')(list.dymListDetDummy, {groupId: list.groupSelected['_id']});
			}
		}
	}
	
});