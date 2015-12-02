angular.module('sbAdminApp').controller('AddMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $base64, $translate, loadImg, toaster, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	var isChangedImg;
	
	if($stateParams.menu) { 
		$scope.isEdit = true;
		$translate('menu.header_panel_edit').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});
		$translate('menu.addpage.update_btn').then(function (updateBtn) {
			$scope.persisBtn = updateBtn;
		});
		
		$scope.menu = $stateParams.menu;
		if(loadImg) {			
			$scope.imageSource = 'data:image/JPEG;base64,' + loadImg.imgBase64;
		}
	} else {
		$translate('menu.header_panel_add').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});		
		$translate('menu.addpage.save_btn').then(function (saveBtn) {
			$scope.persisBtn = saveBtn;
		});

		$scope.menu = {};
		$scope.menu.status = 1;
		$scope.menu.isRecommented = false;
	}
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/menu/saveMenu', {
			name: $scope.menu.name,
			price: $scope.menu.price,
			status: $scope.menu.status,
			isRecommented: $scope.menu.isRecommented,
			menuTypeId: menu.menuTypeId,
			imgContent: $scope.imgUpload && $scope.imgUpload.base64,
			imgName: $scope.imgUpload && $scope.imgUpload.filename
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			$scope.formData.currentPage = 1;
			$scope.formData.status = null;
			$scope.formData.name = null;
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
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/menu/updateMenu', {
			id: $scope.menu.id,
			name: $scope.menu.name,
			price: $scope.menu.price,
			status: $scope.menu.status,
			isRecommented: $scope.menu.isRecommented,
			menuTypeId: menu.menuTypeId,
			isChangedImg: isChangedImg,
			imgContent: $scope.imgUpload && $scope.imgUpload.base64,
			imgName: $scope.imgUpload && $scope.imgUpload.filename
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update User Success');
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
	
	$scope.clear = function() {
		setNull();
	}
	
	function setNull() {
		$scope.menu.name = null;
		$scope.menu.price = null;
		$scope.menu.status = 1;
		$scope.menu.isRecommented = false;		
		$scope.imgUpload = null;
		$('#imgUpload').attr('src', null);
		$("#image").val('');
	}

	$scope.preview = function(element) {		
		isChangedImg = true;
		
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