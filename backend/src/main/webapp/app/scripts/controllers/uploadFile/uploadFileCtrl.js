angular.module('sbAdminApp').controller('UploadFileCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, FileUploader, loadData) {
	console.log(loadData);
	console.log('UploadFileCtrl');
	var uploader;
	var askDetail;
	
	$scope.maxSize = 5;
	$scope.totalItems = loadData.totalItems;
	$scope.periodObj = loadData.lastPeriod;
	$scope.orderFiles = loadData.orderFiles;
	$scope.customerNameLst = loadData.customerNameLst;
	$scope.uploadData = {};
	$scope.formData = {
		currentPage: 1,
		itemsPerPage: 10
	};
	$scope.statuses = [
		{id: 0, title: 'ยังไม่ลงข้อมูล'}, {id: 1, title: 'กำลังลงข้อมูล'}, {id: 2, title: 'ลงข้อมูลเสร็จแล้ว'}
	];
	
	//-------
	$scope.removeItem = function(item) {
		var r = confirm("ยืนยันการลบข้อมูล");
		if (r == false) return;
		
		$http.post(urlPrefix + '/restAct/uploadFile/removeFile',{
			id: item['_id'],
			dealerId: $rootScope.workingOnDealer.id
    	}).then(function(data){
    		var result = data.data;
    		if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return $q.reject(data);
			}
    		
    		$scope.getFiles();
    		if(result.errCode != 0) {
    			console.log(result.errorCode);
    			
    			$ngConfirm({
    			    title: 'ลบรายการที่กำลังบันทึก',
    			    content: "รายการที่ลบ เป็นรายการที่ได้เริ่มบันทึกข้อมูลแล้ว  กรุณาแจ้ง ผู้บันทึก !!!",
    			    type: 'blue',
    			    scope: $scope,
    			    typeAnimated: true,
    			    columnClass: 'col-xs-6 col-xs-offset-5',
    			    buttons: {
    			        ok: {
    			        	text: 'OK',
    			        	btnClass: 'btn-blue'
    			        }
    			    }
    			});	
    		}
    	}, function(response) {
			console.log(response);
		});
	}
	
	$scope.uploadFile = function() {	
		getNames();
	}
	
	$scope.checkNameEmpty = function() {
		if($scope.uploadData.customerName) {
			askDetail.buttons.ok.setDisabled(false);
		} else {
			askDetail.buttons.ok.setDisabled(true);			
		}
	}
	
	$scope.pageChanged = function() {
		$scope.getFiles();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.getFiles();
	}
	
	
	//--------
	$scope.getFiles = function() {
		$http.post(urlPrefix + '/restAct/uploadFile/getFiles',{
			customerName: $scope.formData.customerName,
			status: $scope.formData.status,
    		currentPage: $scope.formData.currentPage,
    		itemsPerPage: $scope.formData.itemsPerPage,
    		dealerId: $rootScope.workingOnDealer.id,
    		periodId: $scope.periodObj['_id']
    	}).then(function(data){
    		var result = data.data;
    		if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return $q.reject(data);
			}
    		
    		$scope.totalItems = result.totalItems;
    		$scope.orderFiles = result.orderFiles;
    		if($scope.totalItems > 0) {    			
    			$scope.customerNameLst = result.customerNameLst;
    		}
    	}, function(response) {
			console.log(response);
		});
	}
	
	function getNames() {
		$http.post(urlPrefix + '/restAct/order/getNames', {
			periodId: $scope.periodObj['_id'],
			dealerId: $rootScope.workingOnDealer.id			
		}).then(function(data) {
			var result = data.data;
			$scope.names = result.orderNameLst;
			$scope.uploadData.customerName = null;
			
	        askDetail = $ngConfirm({
			    title: false,
			    contentUrl: './views/uploadFile/customer_name.html',
			    type: 'blue',
			    scope: $scope,
			    typeAnimated: true,
			    columnClass: 'col-xs-6 col-xs-offset-5',
			    buttons: {
			        ok: {
			        	text: 'Upload',
			        	btnClass: 'btn-blue',
			        	disabled: true,
			        	action: function(scope, button){
			        		for(var x in uploader.queue) {
			        			uploader.queue[x].formData[0].customerName = scope.uploadData.customerName;
			        		}
			        		uploader.uploadAll();	
			        	}
			        },
			        cancel: {
			        	text: 'ยกเลิก'
			        }
			    }
			});
		}, function(response) {
			console.log(response);
		});
	}
	
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/uploadFile/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{
        	periodId: $scope.periodObj['_id'], 
        	dealerId: $rootScope.workingOnDealer.id
        }]
    });
	
	 // FILTERS
    /*uploader.filters.push({
        name: 'customFilter',
        fn: function(item {File|FileLikeObject}, options) {
            return this.queue.length < 10;
        }
    });*/

    // CALLBACKS
    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถนำเข้าได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        
        /*if(response.statusCode == 9999) {
        	$scope.datas = response.files;
        	$scope.totalItems = response.totalItems;
        	
        	$scope.formData.currentPage = 1;
        	$scope.formData.itemsPerPage = 10;
        }*/
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
        $scope.getFiles();
    };
    
    
    
});