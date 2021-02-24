angular.module('sbAdminApp').controller('ProductCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, $ngConfirm, loadData, urlPrefix, roles2, roles3) {	
	
	//---:
	$scope.groupProducts = loadData.products;
	$scope.formData = {package: 1};
	$scope.checkComAll = true;
	
	//---:
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
		
		if(cfObj) {
			cfObj.content = '<p>Take note on <u>' + objData.name + '</u></p><textarea class="form-control" rows="3" style="resize: none;" ng-model="more.note"></textarea>';
			cfObj.buttons.ok.action = function(scope, button){
				scope.$apply(function () {
					objData.note = scope.more.note;
				});
			}
			
			cfObj.open();
		} else {
			cfObj = $ngConfirm({
				title: 'Note',
				content: '<p>Take note on <u>' + objData.name + '</u></p><textarea class="form-control" rows="3" style="resize: none;" ng-model="more.note"></textarea>',
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
