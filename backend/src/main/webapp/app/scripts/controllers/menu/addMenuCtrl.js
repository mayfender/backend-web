angular.module('sbAdminApp').controller('AddMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $window, $base64, $translate, toaster, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	var isEdit = false;
	
	if(!isEdit){
		$translate('menu.header_panel_add').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});		
		$translate('menu.addpage.save_btn').then(function (saveBtn) {
			$scope.persisBtn = saveBtn;
		});
		
		$scope.enabled = 1;
	}else{		
		$translate('menu.header_panel_edit').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});
		$translate('menu.addpage.update_btn').then(function (updateBtn) {
			$scope.persisBtn = updateBtn;
		});
	}

	$scope.setFile = function(element) {
		$scope.currentFile = element.files[0];
		var reader = new FileReader();

		reader.onload = function(event) {
			$scope.imageSource = event.target.result
			$scope.$apply()

		}
		// when the file is read it triggers the onload event above.
		reader.readAsDataURL(element.files[0]);
	}
    
    
    
	
	$scope.testing = function() {
		console.log($scope.uploader);
	}
	
});