angular.module('sbAdminApp').controller('DetailConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster, loadData) {
	
	console.log(loadData);
	$scope.$parent.$parent.url = 'importConf';
	$scope.groupDatas = loadData.groupDatas;
	var colFormats;
	$scope.model = [];

    for (var i = 0; i < $scope.groupDatas.length; ++i) {
    	$scope.model.push({id: $scope.groupDatas[i].id, groupName: $scope.groupDatas[i].name, items:[]});
    	colFormats = loadData.colFormMap[$scope.groupDatas[i].id];
    	
        for (var j = 0; j < colFormats.length; ++j) {
        	$scope.model[i].items.push({label: colFormats[j].columnNameAlias || colFormats[j].columnName, detIsActive: colFormats[j].detIsActive});
        }
    }
	
	$scope.addContainer = function() {
		if(!$scope.gname) return;
		$scope.model.push({id: $scope.model.length + 1, groupName: $scope.gname, items:[]});
	}
	
	$scope.dndDragendContainer = function(message, event) {
		console.log('container');
		console.log($scope.model);
//		updateContainer();
	}
	    
	$scope.dndDragendItem = function(message, event) {
		console.log('item');
		console.log($scope.model);
//		updateItem();
	}
	
	
	
	
	
	
	
	
	
	
	//--------------------------------------------------------: Connecting with Server :----------------------------------------------------
	
	$scope.deleteContainer = function() {
		console.log('deleteContainer');
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