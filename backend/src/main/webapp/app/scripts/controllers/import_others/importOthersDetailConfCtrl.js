angular.module('sbAdminApp').controller('ImportOthersDetailConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, urlPrefix, toaster, loadData) {

	console.log(loadData);
	$scope.groupDatas = loadData.groupDatas;
	$scope.model = [];
	var colFormats;

    for (var i = 0; i < $scope.groupDatas.length; ++i) {
    	$scope.model.push({id: $scope.groupDatas[i].id, name: $scope.groupDatas[i].name, columnFormats:[]});
    	colFormats = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[i].id], 'detOrder');
    	
    	if(!colFormats) continue;
    	
        for (var j = 0; j < colFormats.length; ++j) {
        	$scope.model[i].columnFormats.push({
        		columnName: colFormats[j].columnName, 
        		columnNameAlias: colFormats[j].columnNameAlias,
        		detIsActive: colFormats[j].detIsActive
        	});
        }
    }
	
	$scope.addContainer = function() {
		if(!$scope.gname) return;
		
		var id = Math.floor(Math.random()*90000) + 10000;
		
		outer:while(true) {
			for(x in $scope.model) {
				if($scope.model[x].id == id) {
					id = Math.floor(Math.random()*90000) + 10000;
					continue outer;
				}
			}
			break;
		}
		
		$scope.model.push({id: id, name: $scope.gname, columnFormats:[]});
		updateContainer();
	}
	
	$scope.dndDragendContainer = function(message, event) {
		updateContainer();
	}

	//--------------------------------------------------------: Connecting with Server :----------------------------------------------------
	function updateContainer() {
		var groups = angular.copy($scope.model);
		for(x in groups) {
			delete groups[x].columnFormats;
		}
		
		$http.post(urlPrefix + '/restAct/importMenu/updateGroupDatas', {
			groupDatas: groups,
			productId: $stateParams.productInfo.id,
			menuId: $stateParams.menuInfo.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dndDragendItem = function(message, event) {		
		$http.post(urlPrefix + '/restAct/importMenu/updateColumnFormatDet', {
			colFormGroups: $scope.model,
			productId: $stateParams.productInfo.id,
			menuId: $stateParams.menuInfo.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.isActiveSetting = function(item) {
		console.log(item);
		$http.post(urlPrefix + '/restAct/importMenu/updateColumnFormatDetActive', {
			columnFormat: item,
			productId: $stateParams.productInfo.id,
			menuId: $stateParams.menuInfo.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	//------------------------------------------------------------------------------------------------------------
	
	$scope.dragoverCallback = function(event, index, external, type) {
        $scope.logListEvent('dragged over', event, index, external, type);
        // Disallow dropping in the third row. Could also be done with dnd-disable-if.
        //return index < 10;
        return true
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
        /*console.log(message, '(triggered by the following', event.type, 'event)');
        console.log(event);*/
    };

    $scope.logListEvent = function(action, event, index, external, type) {
        /*var message = external ? 'External ' : '';
        message += type + ' element is ' + action + ' position ' + index;
        $scope.logEvent(message, event);*/
    };
	
});