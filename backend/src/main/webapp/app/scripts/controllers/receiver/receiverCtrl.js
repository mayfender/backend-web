angular.module('sbAdminApp').controller('ReceiverCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('receiver');
	
	$scope.receiver = {
		addEditPanel: 0,
		fieldIndex: 0
	};
	$scope.receiver.formData = {};
	$scope.receiver.list = loadData.receiverList;
	$scope.priceList = {};
	
	$scope.preventOuterEvent = function(e) {
		e.stopPropagation();
	}
	
	$scope.cutOffToggle = function(obj) {
		$http.post(urlPrefix + '/restAct/receiver/cutOffToggle', {
			id: obj.id,
			isCuttingOff: obj.isCuttingOff,
			dealerId: $rootScope.workingOnDealer.id,
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			obj.isCuttingOff = obj.isCuttingOff ? false : true;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	$scope.statusToggle = function(obj) {
		$http.post(urlPrefix + '/restAct/receiver/statusToggle', {
			id: obj.id,
			enabled: obj.enabled,
			dealerId: $rootScope.workingOnDealer.id,
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			obj.enabled = obj.enabled ? false : true;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	$scope.receiver.addEditReceiver = function(panel, obj) {
		$scope.receiver.addEditPanel = panel;
		$scope.receiver.fieldIndex = 0;
		
		if($scope.receiver.addEditPanel == 2) {
			$scope.receiver.formData.receiverId = obj.id;
			$scope.receiver.formData.senderName = obj.senderName;
			$scope.receiver.formData.receiverName = obj.receiverName;
			$scope.receiver.formData.priceListId = obj.priceListId;
		} else {
			initFields();
			$scope.receiver.formData.priceListId = null;
		}
	}
	
	$scope.receiver.chageField = function() {
		if($scope.receiver.fieldIndex == 0) {
			$scope.receiver.fieldIndex = 1;
		} else {
			$scope.receiver.fieldIndex = 0;			
		}
	}
	
	$scope.receiver.saveUpdateReceiver = function() {
		$http.post(urlPrefix + '/restAct/receiver/saveUpdateReceiver', {
			receiverName: $scope.receiver.formData.receiverName,
			senderName: $scope.receiver.formData.senderName,
			priceListId: $scope.receiver.formData.priceListId,
			dealerId: $rootScope.workingOnDealer.id,
			id: $scope.receiver.formData.receiverId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.receiver.addEditPanel = 0;
			getReceiverList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	function getReceiverList() {
		$http.get(urlPrefix + '/restAct/receiver/getReceiverList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.receiver.list = result.receiverList;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getPriceList() {
		$http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id + '&enabled=true').then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.priceList.list = result.priceList;
			
			console.log($scope.priceList.list);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function updateOrder(data) {
		var deferred = $q.defer();
		
		$http.post(urlPrefix + '/restAct/receiver/updateOrder', {
			orderData: data,
			dealerId: $rootScope.workingOnDealer.id
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	function initFields() {
		$scope.receiver.formData.receiverId = null;
		$scope.receiver.formData.senderName = null;
		$scope.receiver.formData.receiverName = null;
	}
	
	initFields();
	getPriceList();
	
	
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
	    	    	            	getReceiverList();
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