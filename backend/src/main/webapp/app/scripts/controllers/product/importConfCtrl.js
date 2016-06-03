angular.module('sbAdminApp').controller('ImportConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	$scope.$parent.headerTitle = 'ตั้งค่ารูปแบบไฟล์';		
	$scope.data = $stateParams.data;
	
	/*$scope.update = function() {
		
		delete $scope.data['createdDateTime'];
		console.log($scope.data);
		
		$http.post(urlPrefix + '/restAct/product/updateDatabaseConf', {
				id: $scope.data.id,
				database: $scope.data.database
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Success');
			$state.go('dashboard.product.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'enabled': $scope.formData.enabled,
				'productName': $scope.formData.productName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}*/
	
	
	
	
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
        console.log(message, '(triggered by the following', event.type, 'event)');
        console.log(event);
    };

    $scope.logListEvent = function(action, event, index, external, type) {
        var message = external ? 'External ' : '';
        message += type + ' element is ' + action + ' position ' + index;
        $scope.logEvent(message, event);
    };

    
    $scope.containers = [];
    var id = 1;
    for (var i = 0; i < 1; ++i) {
    	$scope.containers.push([]);
    	for (var j = 0; j < 30; ++j) {    		
    		$scope.containers[i].push({label: 'Col ' + id++});
    	}
    }

    $scope.$watch('containers', function(containers) {
        $scope.modelAsJson = angular.toJson(containers, true);
    }, true);
	
	
});