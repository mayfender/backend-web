angular.module('sbAdminApp').controller('DetailConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster, loadData) {
	
	console.log(loadData);
	$scope.$parent.$parent.url = 'importConf';
	
	$scope.groupNames = loadData.groupNames;
	var colFormats;
	$scope.model = [];

    for (var i = 0; i < $scope.groupNames.length; ++i) {
    	$scope.model.push([]);
    	colFormats = loadData.colFormMap[$scope.groupNames[i]];
    	
        for (var j = 0; j < colFormats.length; ++j) {
        	$scope.model[i].push({label: colFormats[j].columnNameAlias || colFormats[j].columnName});
        }
    }
	
	$scope.addContainer = function() {
		$scope.model.push([]);
	}
	
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
        console.log(message, '(triggered by the following', event.type, 'event)');
        console.log(event);
    };

    $scope.logListEvent = function(action, event, index, external, type) {
        var message = external ? 'External ' : '';
        message += type + ' element is ' + action + ' position ' + index;
        $scope.logEvent(message, event);
    };
	
});