angular.module('sbAdminApp').controller('MenuTypeCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadMenuType) {
	
	var err_msg;
	var msg_err_delete_mType;
	$scope.menuTypes = loadMenuType.menuTypes;
	$translate('message.err.empty').then(function (mgs) {
		err_msg = mgs;
	});
	$translate('message.err.cann_delete_menu_type').then(function (msg) {
		msg_err_delete_mType = msg;
	});
	
	$scope.addMenuType = function() {
		$scope.inserted = {
			name: ''
	    };
		$scope.menuTypes.push($scope.inserted);
	};
	
	$scope.saveMenuType = function(data, mt, index) {
		 var msg;
		 if(!mt.id) {
			 msg = 'Save User Success';			 
		 } else {
			 msg = 'Update User Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/menuType/saveAndUpdate', {
			id: mt.id,
			name: data.name
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
			
			$scope.menuTypes[index].id = data.data.id;			
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	$scope.cancel = function(index) {
		$scope.menuTypes.splice(index, 1);
	}
	
	$scope.checkName = function(data) {
		if (data == null || data == '') {
			return err_msg;
		}
	};
	
	$scope.removeMenuType = function(index, mt) {
		var deleteUser = confirm('Are you sure you want to delete this Item?');
	    if(!deleteUser) return;
	    
		return $http.get(urlPrefix + '/restAct/menuType/deleteMenuType?id=' + mt.id).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 5000) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType);
				} else {					
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete User Success');
			
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
});