angular.module('sbAdminApp').controller('SearchUserCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, loadUsers, urlPrefix, roles, roles2, roles3) {	
	
	console.log(loadUsers);
	
	$scope.maxSize = 5;
	$scope.$parent.url = 'add';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.data = {};
	$scope.data.users = loadUsers.users;
	$scope.totalItems = loadUsers.totalItems;
	
	if($rootScope.authority == 'ROLE_ADMIN') {		
		$scope.rolesConstant = roles2;		
	}else if($rootScope.authority == 'ROLE_MANAGER') {
		$scope.rolesConstant = roles3;
	} else {
		$scope.rolesConstant = roles;		
	}
	
	$translate('user.header.panel.user_list').then(function (userList) {
		$scope.$parent.headerTitle = userList;
	});
	
	$scope.deleteUser = function(userId) {
		
		var deleteUser = confirm('Are you sure you want to delete this USER?');
	    if(!deleteUser) return;
		
		$http.post(urlPrefix + '/restAct/user/deleteUser', {
			userId: userId,
			userName: $scope.formData.userName,
			role: $scope.formData.role,
			enabled: $scope.formData.enabled,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	currentProduct: $rootScope.workingOnProduct.id,
	    	product: $scope.formData.product
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Delete User Success');
    		$scope.data.users = data.data.users;
    		$scope.totalItems = data.data.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		console.log($scope.formData.product);
		
		$http.post(urlPrefix + '/restAct/user/findUserAll', {
			userNameShow: $scope.formData.userNameShow,
			userName: $scope.formData.userName,
			role: $scope.formData.role,
			enabled: $scope.formData.enabled,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	currentProduct: $rootScope.workingOnProduct.id,
	    	product: $scope.formData.product
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.data.users = data.data.users;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.enabled = null;
		$scope.formData.role = "";
		$scope.formData.userNameShow = null;
		$scope.formData.userName = null;
		$scope.formData.product = null;
		$scope.search();
	}
	
	$scope.editUser = function(user) {
		$state.go('dashboard.user.add', {user: user});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});
