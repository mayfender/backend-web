angular.module('sbAdminApp').controller('ImportOthersConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, urlPrefix, toaster, loadData) {
	
	$scope.containers = [];
	$scope.containers[0] = loadData.columnFormats;
	$scope.mainColumnFormats = loadData.mainColumnFormats;	
	$scope.headerTitle = 'ตั้งค่าหัวตาราง [' + $stateParams.productInfo.productName + ']';		
	$scope.menuName = $stateParams.menuInfo.menuName;
	$scope.contractNoColumnName = loadData.contractNoColumnName;
	$scope.idCardNoColumnName = loadData.idCardNoColumnName;
	$scope.formData = {};
	$scope.$parent.isShowBackBtn = false;
	
	var activeCount = 0;
	for(x in $scope.containers[0]) {
		if($scope.containers[0][x].isActive) {
			activeCount++;
		}
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
	
	$scope.updateNotice = function(item) {
		$http.post(urlPrefix + '/restAct/importMenu/updateNotice', {
			menuId: $stateParams.menuInfo.id,
			productId: $stateParams.productInfo.id,
			columnName: item && item.columnName
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.updateNoticeForms = function(id, noticeForms, isChk) {
    	if(isChk) {
    		noticeForms.push(id);    		
    	} else {
    		var index = noticeForms.indexOf(id);
    		noticeForms.splice(index, 1);
    	}
    	    	
    	$scope.update();
    }
	
	$scope.updateColumnName = function(colName) {
		var params = {
			productId: $stateParams.productInfo.id,
			menuId: $stateParams.menuInfo.id
		}
		
		if(colName == 'idCard') {
			params.idCardNoColumnName = $scope.idCardNoColumnName;
		} else if(colName == 'contactNo') {
			params.contractNoColumnName = $scope.contractNoColumnName;
		}
		
		$http.post(urlPrefix + '/restAct/importMenu/updateColumnName', params).then(function(data) {
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
    
    
    
    var myModal;
    var isDismissModal;
    $scope.noticeList = function(item) {
    	if(!item.noticeForms) item.noticeForms = new Array();
    	
    	$scope.noticeForms = item.noticeForms;
    	
		$http.post(urlPrefix + '/restAct/notice/find', {
			enabled: true,
			currentPage: 1, 
			itemsPerPage: 1000,
			productId: $stateParams.productInfo.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.files = result.files;
			var file;
			for(i in $scope.files) {
				file = $scope.files[i];
				var ch = $filter('filter')($scope.noticeForms, file.id)[0];
				
				if(ch) file.isChk = true;
			}			
			
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('shown.bs.modal', function (e) {
					//--
				});
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
				myModal.on('hidden.bs.modal', function (e) {
					//--
  				});
			} else {			
				myModal.modal('show');
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
    
    $scope.dismissModal = function() {
		if(!myModal) return;
		
		isDismissModal = true;
		myModal.modal('hide');
	}
    
    
	
});