angular.module('sbAdminApp').controller('AddMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $base64, $translate, loadImg, toaster, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	var isChangedImg;
	
	var editor = new nicEditor({fullPanel : true}).panelInstance('area1');	
	
	var err_msg;
	$translate('message.err.empty').then(function (msg) {
		err_msg = msg;
	});
		
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
		
		editor.instanceById('area1').setContent($scope.menu.menuDetailHtml || '');
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
		$scope.menu.menuType = {};
	}
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/menu/saveMenu', {
			name: $scope.menu.name,
			price: $scope.menu.price,
			status: $scope.menu.status,
			isRecommented: $scope.menu.isRecommented,
			menuTypeId: $scope.menu.menuType.id,
			imgContent: $scope.imgUpload && $scope.imgUpload.base64,
			imgName: $scope.imgUpload && $scope.imgUpload.filename,
			menuDetailHtml: editor.instanceById('area1').getContent()
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			$scope.formData.currentPage = 1;
			$scope.formData.status = null;
			$scope.formData.name = null;
			$scope.formData.isRecommented = null;
			$scope.formData.menuTypeId = '';
			$state.go('dashboard.menu.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'status': $scope.formData.status, 
				'name': $scope.formData.name,
				'isRecommented': $scope.formData.isRecommented,
				'menuTypeId': $scope.formData.menuTypeId
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
			menuTypeId: $scope.menu.menuType.id,
			isChangedImg: isChangedImg,
			imgContent: $scope.imgUpload && $scope.imgUpload.base64,
			imgName: $scope.imgUpload && $scope.imgUpload.filename,
			menuDetailHtml: editor.instanceById('area1').getContent()
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
				'name': $scope.formData.name,
				'menuTypeId': $scope.formData.menuTypeId
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
	
	
	$scope.subMenus = {};
	$scope.subMenu = function() {
		$http.get(urlPrefix + '/restAct/subMenu/findByMenuId?menuId=' + $scope.menu.id).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$scope.subMenus = data.data.subMenus;
    		var myModal = $('#myModal').modal();
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.addSubMenu = function() {
		$scope.inserted = {
			name: ''
	    };
		$scope.subMenus.push($scope.inserted);
	};
	
	$scope.saveSubMenu = function(data, mt, index) {
		 var msg;
		 if(!mt.id) {
			 msg = 'Save Sub-Menu Success';			 
		 } else {
			 msg = 'Update Sub-Menu Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/subMenu/saveAndUpdate', {
			id: mt.id,
			name: data.name,
			price: data.price,
			menuId: $scope.menu.id
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.subMenus[index].id = data.data.id;			
			
			$rootScope.systemAlert(data.data.statusCode, msg);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	$scope.removeSubMenu = function(index, mt) {
		var deleteUser = confirm('Are you sure you want to delete this Item?');
	    if(!deleteUser) return;
	    
		return $http.get(urlPrefix + '/restAct/subMenu/deleteSubMenu?id=' + mt.id).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete Sub-Menu Success');
			
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
	$scope.cancel = function(index) {
		$scope.subMenus.splice(index, 1);
	}
	
	$scope.checkName = function(data) {
		if (data == null || data == '') {
			return err_msg;
		}
	};
	
	
});