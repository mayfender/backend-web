angular.module('sbAdminApp').controller('ImportOthersViewSettingConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster, loadData) {
	
	$scope.containers = [];
	$scope.containers[0] = loadData.columnFormats;
	$scope.mainColumnFormats = loadData.mainColumnFormats;	
	$scope.headerTitle = 'ตั้งค่าหัวตาราง [' + $stateParams.productInfo.productName + ']';		
	$scope.menuName = $stateParams.menuInfo.menuName;
	$scope.formData = {};
//	$scope.$parent.iconBtn = 'fa-long-arrow-left';
//	$scope.$parent.url = 'search';
	
	
	var activeCount = 0;
	for(x in $scope.containers[0]) {
		if($scope.containers[0][x].isActive) {
			activeCount++;
		}
	}
	
	$scope.changeColumnLink = function() {
		console.log($scope);
//		console.log($scope.childColumn);
	}
	
	$scope.update = function(item) {
		if($scope.containers[0] == null) return;
		
		if(checkMaxFields(item)) return;
		
		$http.post(urlPrefix + '/restAct/importMenu/updateColumnFormat', {
			productId: $stateParams.productInfo.id,
			menuId: $stateParams.menuInfo.id,
			columnFormats: $scope.containers[0],
			columnName: item && item.columnName,
			isActive: item && item.isActive
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.checkEnabled = function(val) {
		for (x in $scope.containers[0]) {
			$scope.containers[0][x].isActive = (val == 1 ? true : false);
		}
		$scope.update();
	}
	
	$scope.detailDisplayConf = function() {
		$state.go('dashboard.importOthersViewSetting.detailsSetting');
	}
	
	
	//-------------------------------------------------------------------------------------
	
	$scope.dragoverCallback = function(event, index, external, type) {
        $scope.logListEvent('dragged over', event, index, external, type);
        // Disallow dropping in the third row. Could also be done with dnd-disable-if.
        return index < 100;
    };

    $scope.dropCallback = function(event, index, item, external, type, allowedType) {
    	
        $scope.logListEvent('dropped at', event, index, external, type);
        if (external) {
            if (allowedType === 'itemType' && !item.label) return false;
            if (allowedType === 'containerType' && !angular.isArray(item)) return false;
        }
        return item;
    };

    $scope.logEvent = function(message, event) {
//        console.log(message, '(triggered by the following', event.type, 'event)');
//        console.log(event);
    };

    $scope.logListEvent = function(action, event, index, external, type) {
        var message = external ? 'External ' : '';
        message += type + ' element is ' + action + ' position ' + index;
        $scope.logEvent(message, event);
    };
    
    $scope.dndDragend = function(message, event) {
    	$scope.update();
    }

    function checkMaxFields(item) {
		$scope.isOver = false;
		
		if(item) {
			if(item.isActive) {		
				if(activeCount == 30) {
					item.isActive = false;
					$scope.isOver = true;
					return true;
				} else {
					activeCount++;				
				}
			} else {			
				activeCount--;
			}
		}
	}
	
});