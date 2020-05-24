angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $scope, $base64, $http, $translate, $ngConfirm, urlPrefix, loadData) {
	console.log('Order');
	
	$scope.periods = [{id: '1', name: '01/01/2020'},{id: '2', name: '15/01/2020'},{id: '3', name: '01/02/2020'}];
	$scope.formData = {period: $scope.periods[0].id};
	
	$scope.mayfender = 'may';
	
	$scope.addPeriod = function() {
		$ngConfirm({
		    title: 'เพิ่มงวดใหม่',
		    contentUrl: './views/order/addPeriod.html',
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        save: {
		            text: 'บันทึก',
		            btnClass: 'btn-blue',
		            action: function(){
		            	
		            }
		        },
		        close: {
		        	text: 'ยกเลิก',
		        	action: function(){
		            	
		            }
		        }
		    }
		});
	}
	
	
});