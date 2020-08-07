angular.module('sbAdminApp').controller('PriceListCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log('PriceListCtrl');
	console.log(loadData);
	
	$scope.priceList = {
		addEditPanel: 0,
		fieldIndex: 0
	};
	$scope.priceList.formData = {};
	$scope.priceList.list = loadData.priceList;
	
	$scope.priceList.label = [{
		group1: 'ราคา 3 / 2 / 2 ตัวล่าง',
		group2: 'ราคา ลอย 1 ตัว / 4 ตัว / 5 ตัว',
		group3: 'ราคา 3 ตัวโต๊ด',
		group4: 'ราคา วิ่งบน / วิ่งล่าง',
	}, {
		group1: 'เปอร์เซ็น 3 / 2 / 2 ตัวล่าง',
		group2: 'เปอร์เซ็น ลอย 1 ตัว / 4 ตัว / 5 ตัว',		
		group3: 'เปอร์เซ็น 3 ตัวโต๊ด',
		group4: 'เปอร์เซ็น วิ่งบน / วิ่งล่าง',
	}];
	
	$scope.statusToggle = function(e, obj) {
		e.stopPropagation()
		
		$http.post(urlPrefix + '/restAct/receiver/priceListStatusToggle', {
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
	
	$scope.priceList.addEditPriceList = function(panel, obj) {
		$scope.priceList.addEditPanel = panel;
		$scope.priceList.fieldIndex = 0;
		
		if($scope.priceList.addEditPanel == 2) {
			$scope.priceList.formData.priceListId = obj.id;
			$scope.priceList.formData.priceListName = obj.priceListName;
			
			$scope.priceList.sale[0].fieldBon3 = obj.priceBon3;
			$scope.priceList.sale[0].fieldBon2 = obj.priceBon2;
			$scope.priceList.sale[0].fieldLang2 = obj.priceLang2;
			$scope.priceList.sale[0].fieldTod = obj.priceTod;
			$scope.priceList.sale[0].fieldLoy = obj.priceLoy;
			$scope.priceList.sale[0].fieldPare4 = obj.pricePare4;
			$scope.priceList.sale[0].fieldPare5 = obj.pricePare5;
			$scope.priceList.sale[0].fieldRunBon = obj.priceRunBon;
			$scope.priceList.sale[0].fieldRunLang = obj.priceRunLang;
			
			$scope.priceList.sale[1].fieldBon3 = obj.percentBon3;
			$scope.priceList.sale[1].fieldBon2 = obj.percentBon2;
			$scope.priceList.sale[1].fieldLang2 = obj.percentLang2;
			$scope.priceList.sale[1].fieldTod = obj.percentTod;
			$scope.priceList.sale[1].fieldLoy = obj.percentLoy;
			$scope.priceList.sale[1].fieldPare4 = obj.percentPare4;
			$scope.priceList.sale[1].fieldPare5 = obj.percentPare5;
			$scope.priceList.sale[1].fieldRunBon = obj.percentRunBon;
			$scope.priceList.sale[1].fieldRunLang = obj.percentRunLang;
		} else {
			initFields();
		}
	}
	
	$scope.priceList.chageField = function() {
		if($scope.priceList.fieldIndex == 0) {
			$scope.priceList.fieldIndex = 1;
		} else {
			$scope.priceList.fieldIndex = 0;			
		}
	}
	
	$scope.priceList.saveUpdatePriceList = function() {
		$http.post(urlPrefix + '/restAct/receiver/saveUpdatePriceList', {
			priceListName: $scope.priceList.formData.priceListName,
			dealerId: $rootScope.workingOnDealer.id,
			
			id: $scope.priceList.formData.priceListId,
			
			priceBon3: $scope.priceList.sale[0].fieldBon3,
			priceBon2: $scope.priceList.sale[0].fieldBon2,
			priceLang2: $scope.priceList.sale[0].fieldLang2,
			priceTod: $scope.priceList.sale[0].fieldTod,
			priceLoy: $scope.priceList.sale[0].fieldLoy,
			pricePare4: $scope.priceList.sale[0].fieldPare4,
			pricePare5: $scope.priceList.sale[0].fieldPare5,
			priceRunBon: $scope.priceList.sale[0].fieldRunBon,
			priceRunLang: $scope.priceList.sale[0].fieldRunLang,
			
			percentBon3: $scope.priceList.sale[1].fieldBon3,
			percentBon2: $scope.priceList.sale[1].fieldBon2,
			percentLang2: $scope.priceList.sale[1].fieldLang2,
			percentTod: $scope.priceList.sale[1].fieldTod,
			percentLoy: $scope.priceList.sale[1].fieldLoy,
			percentPare4: $scope.priceList.sale[1].fieldPare4,
			percentPare5: $scope.priceList.sale[1].fieldPare5,
			percentRunBon: $scope.priceList.sale[1].fieldRunBon,
			percentRunLang: $scope.priceList.sale[1].fieldRunLang
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.priceList.addEditPanel = 0;
			getPriceList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	function getPriceList() {
		$http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.priceList.list = result.priceList;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function updateOrder(data) {
		var deferred = $q.defer();
		
		$http.post(urlPrefix + '/restAct/receiver/updatePriceListOrder', {
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
		$scope.priceList.formData.priceListId = null;
		$scope.priceList.formData.priceListName = null;
		
		//-- ขาย
		$scope.priceList.sale = [{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy: null,
			fieldPare4: null,
			fieldPare5: null,
			fieldRunBon: null,
			fieldRunLang: null
		},{
			fieldBon3: null,
			fieldBon2: null,
			fieldLang2: null,
			fieldTod: null,
			fieldLoy: null,
			fieldPare4: null,
			fieldPare5: null,
			fieldRunBon: null,
			fieldRunLang: null
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
	    	    	            	getPriceList();
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