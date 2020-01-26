angular.module('sbAdminApp').controller('DymListDetCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, $q, urlPrefix, loadData) {
	
	$scope.itemDets = loadData.dymListDet;
	
	$scope.dymListDetGroup = loadData.dymListDetGroup;
//	if($scope.dymListDetGroup) $scope.dymListDetGroup.unshift({id: undefined, name: ""});
	
	$scope.statuses = [{value: 1, text: 'เปิด'}, {value: 0, text: 'ปิด'}]; 
	$scope.$parent.$parent.isShowBack = true;
	$scope.$parent.$parent.isShowProd = false;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dymList/findListDet', {
			dymListId: $stateParams.id,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.itemDets = data.data.dymListDet;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//------------------------------: Editable :----------------------------------------
	$scope.addItem = function() {
        $scope.inserted = {code: '', desc: '', meaning: '', isPrintNotice: false, enabled: 1};
        $scope.itemDets.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.itemDets) {
    		if($scope.itemDets[i] == item) {
    			$scope.itemDets.splice(i, 1);
    		}
    	}
    }

	$scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymList/deleteListDet?id='+id+'&productId='+ $rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.itemDets.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/dymList/saveListDet', {
			id: item.id,
			code: data.code,
			desc: data.desc,
			groupId: data.groupId,
			meaning: data.meaning,
			isPrintNotice: data.isPrintNotice == null ? false : data.isPrintNotice,
			enabled: JSON.parse(data.enabled),
			isSuspend: JSON.parse(data.isSuspend ? true : false),
			productId: $rootScope.workingOnProduct.id,
			dymListId: $stateParams.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewItem(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	$scope.listDetgroupModal = function() {		
		if(!myModal) {
			myModal = $('#myModal').modal();			
			myModal.on('hide.bs.modal', function (e) {
				if(!isDismissModal) {
					return e.preventDefault();
				}
				isDismissModal = false;
			});
			myModal.on('hidden.bs.modal', function (e) {
				//
			});
		} else {			
			myModal.modal('show');
		}
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.addGroup = function() {
        $scope.insertedGroup = {name: ''};
        $scope.dymListDetGroup.push($scope.insertedGroup);
    };
    
    $scope.saveGroup = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/dymList/saveGroup', {
			id: item.id,
			name: data.name,
			productId: $rootScope.workingOnProduct.id,
			dymListId: $stateParams.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewMenu(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.cancelNewMenu(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
    $scope.cancelNewGroup = function(item) {
    	for(i in $scope.dymListDetGroup) {
    		if($scope.dymListDetGroup[i] == item) {
    			$scope.dymListDetGroup.splice(i, 1);
    		}
    	}
    }
    
    $scope.removeGroup = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymList/deleteGroup?id='+id+'&productId='+ $rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.dymListDetGroup.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	function updateOrder(data) {
		var deferred = $q.defer();
		
		$http.post(urlPrefix + '/restAct/fieldSetting/updateOrder', {
			data: data,
			collectionName: 'dymListDet',
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			deferred.resolve(result);
		}, function(response) {
			deferred.reject(response);
		});    
		return deferred.promise;
	}
	
	$scope.$watch('$viewContentLoaded', 
    		function() { 
    	        $timeout(function() {
	    	        $("#tbSortable").sortable({
	    	    	        items: 'tbody > tr',
	    	    	        cursor: 'pointer',
	    	    	        axis: 'y',
	    	    	        placeholder: "highlight",
	    	    	        dropOnEmpty: false,
	    	    	        start: function (e, ui) {
	    	    	            ui.item.addClass("selected");
	    	    	        },
	    	    	        stop: function (e, ui) {
	    	    	        	ui.item.removeClass("selected");
	    	    	        	
	    	    	        	//----------------: Check Cancel [:1] :-------------
	    	    	        	if(!e.cancelable) return;
	    	    	        	
	    	    	            var dataArr = new Array();
	    	    	            $(this).find("tr").each(function (index) {
	    	    	                if (index > 0) {
	    	    	                	dataArr.push({
	    	    	                		id: $(this).find("td").eq(0).attr('id'),
	    	    	                		order: index
	    	    	                	});
	    	    	                }
	    	    	            });
	    	    	            
	    	    	            //-------------: Call updateOrder :----------------------
	    	    	            updateOrder(dataArr).then(function(response) {
	    	    	            	$scope.search();
	    	    	            }, function(response) {
	    	    	                $rootScope.systemAlert(response.status);
	    	    	            });
	    	    	            //-----------------------------------
	    	    	        }
	    	        }); 
	    	        
	    	        //------------------: Press ESC to cancel sorting [:1] :---------------------
	    	        $( document ).keydown(function( event ) {
	    	        	if ( event.keyCode === $.ui.keyCode.ESCAPE ) {
	    	        		$("#tbSortable").sortable( "cancel" );
	    	        	}
	    	        });
	    	        
    	        	
    	    },0);    
    }); //$viewContentLoaded
	
});