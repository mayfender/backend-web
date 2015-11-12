angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $scope, $http, $stateParams, $modal, toaster, urlPrefix) {
	
	$scope.items = [{itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'1', unitPrice:'4,500', price:'Baht/Unit/Month'},
	                {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'2', unitPrice:'1,500', price:'Baht/Unit/Month'},
	                {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'2', unitPrice:'1,500', price:'Baht/Unit/Month'},
	                {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'2', unitPrice:'1,500', price:'Baht/Unit/Month'},
	                {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'2', unitPrice:'1,500', price:'Baht/Unit/Month'},
	                {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'2', unitPrice:'1,500', price:'Baht/Unit/Month'}];
	
	
	

	$scope.open = function() {	
		var modalInstance = $modal.open({
			animation : true,
			templateUrl : 'views/order/modal.html',
			controller : 'ModalInstanceCtrl',
			resolve : {
				items : function() {
					return $scope.items;
				}
			}
		});
	
		modalInstance.result.then(function(items) {
			$scope.items.push(items);
		}, function() {
			console.log('Modal dismissed at: ' + new Date());
		});
	};

});




//--------
angular.module('sbAdminApp').controller('ModalInstanceCtrl', function ($scope, $modalInstance, items) {
	
	var a = {itemName:'+Fax', itemDetail:'ความละเอียด 1200 dpi', amount:'1', unitPrice:'4,500', price:'Baht/Unit/Month'};
	
	$scope.ok = function () {
		$modalInstance.close(a);
	};

	$scope.cancel = function () {
		$modalInstance.dismiss('cancel');
	};
	  
});

