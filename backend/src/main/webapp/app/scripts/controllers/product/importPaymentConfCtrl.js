angular.module('sbAdminApp').controller('ImportPaymentConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster, loadData) {
	
	$scope.containers = [];
	$scope.containers[0] = loadData.columnFormats;
	$scope.contractNoColumnName = loadData.contractNoColumnName;
	$scope.idCardNoColumnName = loadData.idCardNoColumnName;
	$scope.balanceColumnName = loadData.balanceColumnName;
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.$parent.headerTitle = 'ตั้งค่าหัวตาราง [' + $stateParams.productName + ']';		
	
	$scope.update = function(item) {
		if($scope.containers[0] == null) return;
		
		$http.post(urlPrefix + '/restAct/product/updateColumnFormat', {
			id: $stateParams.id,
			columnFormats: $scope.containers[0],
			columnName: item && item.columnName,
			isActive: item && item.isActive,
			isPayment: true
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.updateColumnName = function(colName) {
		var params = {productId: $stateParams.id, isPayment: true}
		
		if(colName == 'idCard') {
			params.idCardNoColumnName = $scope.idCardNoColumnName;
		} else if(colName == 'contactNo') {
			params.contractNoColumnName = $scope.contractNoColumnName;
		}
		
		$http.post(urlPrefix + '/restAct/product/updateColumnName', params).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	
});