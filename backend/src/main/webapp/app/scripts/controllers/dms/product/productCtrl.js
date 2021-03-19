angular.module('sbAdminApp').controller('ProductCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, $ngConfirm, loadData, urlPrefix, roles2, roles3) {	
	
	//---:
	$scope.groupProducts = loadData.products;
	$scope.formData = {};
	$scope.checkComAll = true;
	
	//---:
	$scope.createInvoice = function() {
		var dataObj = new Array();
		var gObj, pObj, items;
		var itemObj;
		for(var x in $scope.groupProducts) {
			gObj = $scope.groupProducts[x];
			items = new Array();
			
			for(var j in gObj.products) {
				pObj = gObj.products[j];
				
				if(!pObj.check || pObj.isPaid) continue;
				
				//---:
				itemObj = {
					name: pObj.name,
					packageId: pObj.package,
					perMn: pObj.perMn,
					note: pObj.note
				};
				if(pObj.price) itemObj.price = parseInt(pObj.price);
				
				items.push(itemObj);
			}
			
			if(items.length == 0) continue;
			dataObj.push({
				name: gObj.name,
				items: items
			});
		}
		
		$http.post(urlPrefix + '/restAct/dms/createInvoice', {
			invoiceData: dataObj
		}).then(function(data){
			var result = data.data;
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return $q.reject(data);
    		}
	
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dms/getProducts', {
			name: $scope.formData.name,
			packageId: $scope.formData.package
		}).then(function(data){
			var result = data.data;
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return $q.reject(data);
    		}
	
    		$scope.checkComAll = true;
    		$scope.groupProducts = result.products;
    		initCheck($scope.groupProducts);
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
	    });	
	}
	
	//---: new items
	$scope.addNewItem = function(gp) {
		gp.products.push({name: gp.newItem, check: true, perMn: false});
		gp.newItem = null;
		gp.newItemMode = null;
	}
	$scope.deleteNew = function(i, gp) {
		gp.products.splice(i, 1);
	}
	//---: new items
	
	$scope.toggleComAll = function() {
		initCheck($scope.groupProducts);
	}
	$scope.toggleCom = function(obj) {
		if(obj.check) {
			var isCheckAll = true;
			var gObj;
			for(var x in $scope.groupProducts) {
				gObj = $scope.groupProducts[x];
				if(!gObj.check) {
					$scope.checkComAll = false;
					isCheckAll = false;
					break;
				}
			}
			if(isCheckAll) {
				$scope.checkComAll = true;
			}
		} else {
			$scope.checkComAll = false;
		}
		
		//---
		obj.products.forEach(function (o) {
			o.check = obj.check;
		});
	}
	
	$scope.togglePro = function(com, prod) {
		if(prod.check) {
			var isCheckAll = true;
			var obj;
			for(var x in com.products) {
				obj = com.products[x];
				if(!obj.check) {
					com.check = false;
					isCheckAll = false;
					break;
				}
			}
			if(isCheckAll) {
				com.check = true;			
				$scope.toggleCom(com);
			}
		} else {
			com.check = false;
			$scope.checkComAll = false;
		}
	}
	
	var cfObj;
	$scope.takeNote = function(objData) {
		$scope.more = {note: objData.note};
		$scope.more.name = objData.name;
		$scope.more.perMn = objData.perMn;
		$scope.more.price = objData.price;
		
		if(cfObj) {
			cfObj.contentUrl = './views/dms/product/more_setting.html',
			cfObj.buttons.ok.action = function(scope, button){
				scope.$apply(function () {
					objData.note = scope.more.note;
					objData.perMn = scope.more.perMn;
					objData.price = scope.more.price;
				});
			}
			
			cfObj.open();
		} else {
			cfObj = $ngConfirm({
				title: 'ตั้งค่าอื่นๆ',
				contentUrl: './views/dms/product/more_setting.html',
				type: 'blue',
				closeIcon: true,
				scope: $scope,
				typeAnimated: true,
				columnClass: 'col-xs-8 col-xs-offset-2',
				buttons: {
					ok: {
						text: 'OK',
						btnClass: 'btn-blue',
						keys: ['enter'],
						action: function(scope, button){
							scope.$apply(function () {
								objData.note = scope.more.note;
								objData.perMn = scope.more.perMn;
								objData.price = scope.more.price;
							});
						}
					}
				}
			});	
		}
	}
	
	//---
	function initCheck(arrObj) {
		arrObj.forEach(function (obj) {
			obj.check = $scope.checkComAll;
			obj.products.forEach(function (obj) {
				obj.check = $scope.checkComAll;
				obj.perMn = obj.package == 1 ? true : false;
			});
		});			
	}
	
	//---
	initCheck($scope.groupProducts);
	
	
	
	
	//-------------------------------------------------------
	angular.element(document).ready(function () {
		$("input[name='name']").typeWatch({
			  wait: 750, // 750ms
			  highlight: true,
			 /* captureLength: 3,*/
			  callback: function(value) {
				  $scope.search();
			  }
		});
    });
	
});
