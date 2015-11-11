angular.module('sbAdminApp').controller('SearchUserCtrl', function($rootScope, $scope, $http, $state, $translate, loadUsers, urlPrefix) {	
	
	$scope.maxSize = 5;
	$scope.totalItems = loadUsers.totalItems;
	$scope.$parent.url = 'add';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.data = {};
	$scope.data.users = loadUsers.users;
	
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
			status: $scope.formData.status,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Delete User Success');
    		$scope.data.users = data.data.users;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/user/findUserAll', {
			userName: $scope.formData.userName,
			role: $scope.formData.role,
			status: $scope.formData.status,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage
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
