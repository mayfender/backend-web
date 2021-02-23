angular.module('sbAdminApp').controller('ProductCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, $ngConfirm, loadData, urlPrefix, roles2, roles3) {	
	
	//---:
	$scope.groupProducts = loadData.products;
	$scope.formData = {package: 1};
	
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
	
    		$scope.groupProducts = result.products;
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
	    });	
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
