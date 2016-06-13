angular.module('sbAdminApp').controller('TaskDetailCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.taskDetails = loadData.taskDetails;	
	$scope.totalItems = loadData.totalItems;
	$scope.noOwnerCount = loadData.noOwnerCount;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd/MM/yyyy";
	$scope.assignMethods = [{id: 1, methodName: 'แบบสุ่ม'}, {id: 2, methodName: 'แบบดูประสิทธิภาพ'}];
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			taskFileId: $stateParams.taskFileId,
			productId: $stateParams.productId,
			columnName: $scope.column,
			order: $scope.order
		}).then(function(data) {
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
	
	$scope.isActiveClick = function(obj) {
		isActiveToggle(obj);
		
		$http.post(urlPrefix + '/restAct/taskDetail/updateTaskIsActive', {
			id: obj.id,
			isActive: obj.sys_isActive.status,
			productId: $stateParams.productId,
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				
				isActiveToggle(obj);
				
				return;
			}
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
		$http.get(urlPrefix + '/restAct/user/getUserByProductToAssign?productId=' + $stateParams.productId).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.users = result.users;
			$scope.isSelectAllUsers = true;
			$scope.selectAllUsersCheckBox();			
			
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	    
	    for (x in $scope.users) {
			if($scope.users[x].isSelectUser) {
				isSelected = true;
				break;
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
			
			$scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	
});