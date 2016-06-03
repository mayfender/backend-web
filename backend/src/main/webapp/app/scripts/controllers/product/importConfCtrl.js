angular.module('sbAdminApp').controller('ImportConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.$parent.headerTitle = 'ตั้งค่ารูปแบบไฟล์นำเข้า';		
	$scope.id = $stateParams.id;
	
	$scope.update = function() {		
		$http.post(urlPrefix + '/restAct/product/updateColumnFormat', {
				id: $scope.id,
//				columFormats: angular.toJson($scope.containers[0], true)
				columFormats: $scope.containers[0]
		}).then(function(data) {
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

    
    $scope.containers = [[{columnName: 'aa'}]];
    /*for (var i = 0; i < 1; ++i) {
    	$scope.containers.push([]);
    	for (var j = 0; j < 25; ++j) {    		
    		$scope.containers[i].push({columnName: null});
    	}
    }*/
    
    $scope.dndDragend = function(message, event) {
    	$scope.update();
    }

    $scope.$watch('containers[0]', function(containers) {
        $scope.modelAsJson = angular.toJson(containers, true);
    }, true);
	
	
});