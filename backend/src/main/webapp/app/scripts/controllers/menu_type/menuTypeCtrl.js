angular.module('sbAdminApp').controller('MenuTypeCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadMenuType) {
	
	var err_msg;
	var msg_err_delete_mType;
	var msg_err_delete_mType_parent;
	$scope.menuTypes = loadMenuType.menuTypes;
	$translate('message.err.empty').then(function (mgs) {
		err_msg = mgs;
	});
	$translate('message.err.cann_delete_menu_type').then(function (msg) {
		msg_err_delete_mType = msg;
	});
	$translate('message.err.cann_delete_menu_type_parent').then(function (msg) {
		msg_err_delete_mType_parent = msg;
	});
	
	$scope.addMenuType = function() {
		$scope.inserted = {
			name: '',
			isEnabled: true
	    };
		$scope.menuTypes.push($scope.inserted);
	};
	
	$scope.addChildMenuType = function(mt) {
		if(!mt.childs) mt.childs = [];
		
		$scope.childInserted = {
			name: '',
			isEnabled: true
	    };
		mt.childs.push($scope.childInserted);
	};
	
	$scope.statuses = [
	                    {value: 0, text: 'ปิด'},
	                    {value: 1, text: 'เปิด'}
	                  ]; 
	
	$scope.saveMenuType = function(data, mt, index) {
		 var msg;
		 if(!mt.id) {
			 msg = 'Save Menu Type Success';			 
		 } else {
			 msg = 'Update Menu Type Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/menuType/saveAndUpdate', {
			id: mt.id,
			name: data.name,
			isEnabled: (data.isEnabled == 1 || data.isEnabled == true) ? true : false 
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
	
	$scope.saveMenuTypeChild = function(data, mt, ch, index) {
		 var msg;
		 if(!ch.id) {
			 msg = 'Save Menu Type Child Success';			 
		 } else {
			 msg = 'Update Menu Type Child Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/menuType/saveAndUpdate', {
			id: ch.id,
			parentId: mt.id,
			name: data.name,
			isEnabled: (data.isEnabled == 1 || data.isEnabled == true) ? true : false 
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
			
			mt.childs[index].id = data.data.id;			
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	
	$scope.cancel = function(index) {
		$scope.menuTypes.splice(index, 1);
	}
	
	$scope.cancelChild = function(index, childs) {
		childs.splice(index, 1);
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
				} else if(data.data.statusCode == 5001) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType_parent);
				} else {					
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete Menu Type Success');
			
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
	$scope.removeMenuTypeChild = function(index, mt, ch) {
		var deleteUser = confirm('Are you sure you want to delete this Item?');
	    if(!deleteUser) return;
	    
		return $http.get(urlPrefix + '/restAct/menuType/deleteMenuType?id=' + ch.id).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 5000) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType);
				} else {					
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete Menu Type Success');
			
			mt.childs.splice(index, 1);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
	
});