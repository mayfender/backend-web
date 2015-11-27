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
		
		$scope.status = 1;
	}else{		
		$translate('menu.header_panel_edit').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});
		$translate('menu.addpage.update_btn').then(function (updateBtn) {
			$scope.persisBtn = updateBtn;
		});
	}
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/menu/saveMenu', {
			name: $scope.menuName,
			price: $scope.menuPrice,
			status: $scope.status,
			isRecommented: $scope.isRecommented,
			imgContent: $scope.imgUpload && $scope.imgUpload.base64,
			imgName: $scope.imgUpload && $scope.imgUpload.filename
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 2001) {
					$translate('message.err.username_show_same').then(function (msg) {
						$scope.existingUserShowErrMsg = msg;
					});
				}else if(data.data.statusCode == 2000) {
					$translate('message.err.username_same').then(function (msg) {
						$scope.existingUserErrMsg = msg;
					});
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			$scope.formData.currentPage = 1;
			$scope.formData.status = null;
			$scope.formData.userName = null;
			$state.go('dashboard.menu.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'status': $scope.formData.status, 
				'name': $scope.formData.name
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	
	
	
	
	
	
	
	

	$scope.preview = function(element) {		
		if (element.files && element.files[0]) {
			$scope.currentFile = element.files[0];
			var reader = new FileReader();
	
			reader.onload = function(event) {
				$scope.imageSource = event.target.result;	
			}
			// when the file is read it triggers the onload event above.
			reader.readAsDataURL(element.files[0]);
			$scope.$apply();
		} else {
			$scope.imgUpload = null;
			$('#imgUpload').attr('src', null);
		}
		
	}
	
	
});