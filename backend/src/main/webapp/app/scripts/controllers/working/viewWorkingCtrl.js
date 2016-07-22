angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.taskDetail = loadData.taskDetail;
	$scope.groupDatas = loadData.groupDatas;
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	var isFirstTimeWorkTab = true;
	var taskDetailId = $stateParams.id;
	lastGroupActive.btnActive = true;
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	$scope.tabActionMenus = [{id: 1, name: 'ผลการติดตาม', url: './views/working/tab_1.html', btnActive: true}, 
	                         {id: 2, name: 'เบอร์ติดต่อ', url: './views/working/tab_2.html'}, 
	                         {id: 3, name: 'ประวัติการนัดชำระ', url: './views/working/tab_3.html'}, 
	                         {id: 4, name: 'payment', url: './views/working/tab_4.html'}, 
	                         {id: 5, name: 'บัญชีพ่วง', url: './views/working/tab_5.html'},
	                         {id: 6, name: 'ข้อมูลงาน', url: './views/working/tab_6.html'}];
	$scope.lastTabActionMenuActive = $scope.tabActionMenus[0];
	
	$scope.view = function(id) {
		taskDetailId = id;
		console.log('view child');
		$scope.idActive = id;
		$http.post(urlPrefix + '/restAct/taskDetail/view', {
    		id: id,
    		productId: $localStorage.setting.currentProduct	
    	}).then(function(data){
    		var result = data.data;
    		
    		if(result.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
    
    		loadData = result;
    		
    		if(lastGroupActive.menu) {
    			relatedData = loadData.relatedData[lastGroupActive.menu];
    			$scope.taskDetail = relatedData.othersData;
    		} else {
    			$scope.taskDetail = loadData.taskDetail;    			
    		}
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
    	});
	}
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1) return;
		var fields;
		
		if(group.menu) {
			relatedData = loadData.relatedData[group.menu];
			$scope.taskDetail = relatedData.othersData;
			fields = relatedData.othersColFormMap[group.id];
		} else {
			$scope.taskDetail = loadData.taskDetail;
			fields = loadData.colFormMap[group.id];
		}
		
		$scope.fieldName = $filter('orderBy')(fields, 'detOrder');			
		lastGroupActive.btnActive = false;
		lastGroupActive = group;
		group.btnActive = true;
	}
	
	for(x in loadData.relatedData) {
		relatedData = loadData.relatedData[x];
		othersGroupDatas = relatedData.othersGroupDatas;
		
		for(i in othersGroupDatas) {
			othersGroupDatas[i].menu = x;
		}
		
		$scope.groupDatas = $scope.groupDatas.concat(othersGroupDatas);		
	}
	
	$scope.changeTabAction = function(menu) {
		
		if(menu.id == 6 && isFirstTimeWorkTab) {
			$scope.formData.itemsPerPage = 5;
			$scope.search();
			isFirstTimeWorkTab = false;
		}
		
		$scope.lastTabActionMenuActive.btnActive = false;
		$scope.lastTabActionMenuActive = menu;
		menu.btnActive = true;
	}
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	var address;
	$scope.noticeMenu = function(addr) {
		address = addr;
		
		$http.post(urlPrefix + '/restAct/notice/find', {
			isInit: false,
			enabled: true,
			currentPage: 1, 
			itemsPerPage: 1000,
			productId: $localStorage.setting.currentProduct	
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			console.log(data.data);
			$scope.files = data.data.files;
		
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
				myModal.on('hidden.bs.modal', function (e) {
					//
  				});
			} else {			
				myModal.modal('show');
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	//------------------------------------------------
	
	$scope.printNotice = function(id) {
		$http.post(urlPrefix + '/restAct/notice/download', {
			id: id,
			taskDetailId: taskDetailId,
			productId: $localStorage.setting.currentProduct,
			address: address,
			isFillTemplate: true
		}, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
				
			var type = fileName.endsWith('.doc') ? 'application/msword' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
			var file = new Blob([data.data], {type: type});
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
	        $scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});