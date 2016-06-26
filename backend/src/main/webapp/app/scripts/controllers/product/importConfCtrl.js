angular.module('sbAdminApp').controller('ImportConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster, loadData) {
	
	$scope.containers = [];
	$scope.containers[0] = loadData.columnFormats;
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.$parent.headerTitle = 'ตั้งค่าหัวตาราง [' + $stateParams.productName + ']';		
	
	var activeCount = 0;
	for(x in $scope.containers[0]) {
		if($scope.containers[0][x].isActive) {
			activeCount++;
		}
	}
	
	$scope.update = function(item) {
		if($scope.containers[0] == null) return;
		
		if(checkMaxFields(item)) return;
		
		$http.post(urlPrefix + '/restAct/product/updateColumnFormat', {
			id: $stateParams.id,
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

    /*$scope.$watch('containers[0]', function(containers) {
        $scope.modelAsJson = angular.toJson(containers, true);
    }, true);*/
	
    //------------------------: context menu :-------------------------------
    
    $scope.selected = 'None';
    $scope.items = [
        { name: 'John', otherProperty: 'Foo' },
        { name: 'Joe', otherProperty: 'Bar' }
    ];

    $scope.menuOptions = [
        ['ลบ', function ($itemScope) {
        	console.log($itemScope.$index);
            $scope.items.splice($itemScope.$index, 1);
        }]
    ];
    
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
    
    //--------------: Tabs :----------------
    
    $scope.tabs = [
                   { title:'ข้อมูลหลัก', content:'Dynamic content 1', active: true },
                   { title:'ข้อมูลหลัก 2', content:'Dynamic content 2'}
                 ];
	
});