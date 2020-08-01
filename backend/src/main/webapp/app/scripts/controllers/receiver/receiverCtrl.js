angular.module('sbAdminApp').controller('ReceiverCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('receiver');
	
	$scope.receiver = {
		addEditPanel: 0,
		fieldIndex: 0
	};
	$scope.receiver.formData = {};
	$scope.receiver.list = loadData.receiverList;
	
	$scope.receiver.label = [{
		group1: 'ราคา 3 / 2 / 2 ตัวล่าง',
		group2: 'ราคา ลอย 1 ตัว / 4 ตัว / 5 ตัว',
		group3: 'ราคา 3 ตัวโต๊ด',
	}, {
		group1: 'เปอร์เซ็น 3 / 2 / 2 ตัวล่าง',
		group2: 'เปอร์เซ็น ลอย 1 ตัว / 4 ตัว / 5 ตัว',		
		group3: 'เปอร์เซ็น 3 ตัวโต๊ด',
	}];
	
	$scope.statusToggle = function(e, obj) {
		e.stopPropagation()
		
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
			
			$scope.receiver.pass[0].fieldBon3 = obj.passPriceBon3;
			$scope.receiver.pass[0].fieldBon2 = obj.passPriceBon2;
			$scope.receiver.pass[0].fieldLang2 = obj.passPriceLang2;
			$scope.receiver.pass[0].fieldTod = obj.passPriceTod;
			$scope.receiver.pass[0].fieldLoy1 = obj.passPriceLoy1;
			$scope.receiver.pass[0].fieldLoy4 = obj.passPriceLoy4;
			$scope.receiver.pass[0].fieldLoy5 = obj.passPriceLoy5;
			
			$scope.receiver.pass[1].fieldBon3 = obj.passPerBon3;
			$scope.receiver.pass[1].fieldBon2 = obj.passPerBon2;
			$scope.receiver.pass[1].fieldLang2 = obj.passPerLang2;
			$scope.receiver.pass[1].fieldTod = obj.passPerTod;
			$scope.receiver.pass[1].fieldLoy1 = obj.passPerLoy1;
			$scope.receiver.pass[1].fieldLoy4 = obj.passPerLoy4;
			$scope.receiver.pass[1].fieldLoy5 = obj.passPerLoy5;
			
			$scope.receiver.sale[0].fieldBon3 = obj.salePriceBon3;
			$scope.receiver.sale[0].fieldBon2 = obj.salePriceBon2;
			$scope.receiver.sale[0].fieldLang2 = obj.salePriceLang2;
			$scope.receiver.sale[0].fieldTod = obj.salePriceTod;
			$scope.receiver.sale[0].fieldLoy1 = obj.salePriceLoy1;
			$scope.receiver.sale[0].fieldLoy4 = obj.salePriceLoy4;
			$scope.receiver.sale[0].fieldLoy5 = obj.salePriceLoy5;
			
			$scope.receiver.sale[1].fieldBon3 = obj.salePerBon3;
			$scope.receiver.sale[1].fieldBon2 = obj.salePerBon2;
			$scope.receiver.sale[1].fieldLang2 = obj.salePerLang2;
			$scope.receiver.sale[1].fieldTod = obj.salePerTod;
			$scope.receiver.sale[1].fieldLoy1 = obj.salePerLoy1;
			$scope.receiver.sale[1].fieldLoy4 = obj.salePerLoy4;
			$scope.receiver.sale[1].fieldLoy5 = obj.salePerLoy5;
		} else {
			initFields();
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
			dealerId: $rootScope.workingOnDealer.id,
			
			id: $scope.receiver.formData.receiverId,
			
			//Pass
			passPriceBon3: $scope.receiver.pass[0].fieldBon3,
			passPriceBon2: $scope.receiver.pass[0].fieldBon2,
			passPriceLang2: $scope.receiver.pass[0].fieldLang2,
			passPriceTod: $scope.receiver.pass[0].fieldTod,
			passPriceLoy1: $scope.receiver.pass[0].fieldLoy1,
			passPriceLoy4: $scope.receiver.pass[0].fieldLoy4,
			passPriceLoy5: $scope.receiver.pass[0].fieldLoy5,
			
			passPerBon3: $scope.receiver.pass[1].fieldBon3,
			passPerBon2: $scope.receiver.pass[1].fieldBon2,
			passPerLang2: $scope.receiver.pass[1].fieldLang2,
			passPerTod: $scope.receiver.pass[1].fieldTod,
			passPerLoy1: $scope.receiver.pass[1].fieldLoy1,
			passPerLoy4: $scope.receiver.pass[1].fieldLoy4,
			passPerLoy5: $scope.receiver.pass[1].fieldLoy5,
			
			//Sale
			salePriceBon3: $scope.receiver.sale[0].fieldBon3,
			salePriceBon2: $scope.receiver.sale[0].fieldBon2,
			salePriceLang2: $scope.receiver.sale[0].fieldLang2,
			salePriceTod: $scope.receiver.sale[0].fieldTod,
			salePriceLoy1: $scope.receiver.sale[0].fieldLoy1,
			salePriceLoy4: $scope.receiver.sale[0].fieldLoy4,
			salePriceLoy5: $scope.receiver.sale[0].fieldLoy5,
			
			salePerBon3: $scope.receiver.sale[1].fieldBon3,
			salePerBon2: $scope.receiver.sale[1].fieldBon2,
			salePerLang2: $scope.receiver.sale[1].fieldLang2,
			salePerTod: $scope.receiver.sale[1].fieldTod,
			salePerLoy1: $scope.receiver.sale[1].fieldLoy1,
			salePerLoy4: $scope.receiver.sale[1].fieldLoy4,
			salePerLoy5: $scope.receiver.sale[1].fieldLoy5
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
	
	function updateOrder(data) {
		var deferred = $q.defer();
		
		data.dealerId = $rootScope.workingOnDealer.id;
		$http.post(urlPrefix + '/restAct/receiver/updateOrder', {
			orderData: data
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
		
		//-- ส่ง
		$scope.receiver.pass = [{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy1: null,
			fieldLoy4: null,
			fieldLoy5: null
		},{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy1: null,
			fieldLoy4: null,
			fieldLoy5: null
		}];
		
		//-- ขาย
		$scope.receiver.sale = [{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy1: null,
			fieldLoy4: null,
			fieldLoy5: null
		},{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy1: null,
			fieldLoy4: null,
			fieldLoy5: null
		}];
	}
	
	initFields();
	
	
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