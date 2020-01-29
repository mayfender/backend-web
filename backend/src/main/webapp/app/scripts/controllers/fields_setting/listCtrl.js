angular.module('sbAdminApp').controller('FieldsSettingListCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, $timeout, $q, urlPrefix, roles, roles2, roles3, toaster, loadData) {
	
	$scope.$parent.headerTitle = 'Fields Setting List';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';	
	$scope.items = loadData.fieldSettings;
	$scope.statuses = [{value: 1, text: 'Enable'}, {value: 0, text: 'Disable'}];
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/fieldSetting/findList', {
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items = result.fieldSettings;
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.saveItem = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/fieldSetting/saveList', {
			id: item.id,
			name: data.name,
			alias: data.alias,
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
	    
	    $http.get(urlPrefix + '/restAct/fieldSetting/deleteList?id='+id+'&productId='+
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
			collectionName: 'fieldSetting',
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
    
    $scope.gotoSetting1 = function(id, name) {
    	$state.go('dashboard.fieldsSetting.list.upload', {id: id, name: name});
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