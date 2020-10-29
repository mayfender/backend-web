angular.module('sbAdminApp').controller('DymListListCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, $timeout, $q, urlPrefix, roles, roles2, roles3, toaster, loadData) {
	
	$scope.$parent.headerTitle = 'แสดง dynamic list';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.isShowProd = true;
	$scope.$parent.menu = 1;
	
	$scope.items = loadData.dymList;
	$scope.statuses = [{value: 1, text: 'เปิด'}, {value: 0, text: 'ปิด'}]; 
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dymList/findList', {
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items = result.dymList;
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.saveItem = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/dymList/saveList', {
			id: item.id,
			name: data.name,
			columnName: data.columnName,
			fieldName: data.fieldName,
			relatedFieldName: data.relatedFieldName,
			order: data.order,
			enabled: JSON.parse(data.enabled),
			productId: $rootScope.workingOnProduct.id
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
	
	$scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymList/deleteList?id='+id+'&productId='+
	    		$rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	function updateOrder(data) {
		var deferred = $q.defer();
		
		$http.post(urlPrefix + '/restAct/fieldSetting/updateOrder', {
			data: data,
			collectionName: 'dymList',
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
	
	$scope.addItem = function() {
        $scope.inserted = {name: '', enabled: 1};
        $scope.items.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }
    
    $scope.gotoDet = function(id) {
    	$state.go('dashboard.dymList.list.listDet', {id: id, productId: $rootScope.workingOnProduct.id});
    }
    
    $scope.$parent.changeProduct = function(prod) {
    	if(prod == $rootScope.workingOnProduct) return;
		
    	$rootScope.workingOnProduct = prod;
		$scope.search();
	}
    
    $scope.$parent.navigate = function(menu) {
    	$scope.$parent.menu = menu;
    	
    	if(menu == 1) {
    		$state.go('dashboard.dymList.list', {});
    	} else if (menu == 2) {
    		$state.go('dashboard.dymList.list.payType', {});
    	} else if (menu == 3) {
    		$state.go('dashboard.dymList.list.search', {});
    	}
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
    });
	
});