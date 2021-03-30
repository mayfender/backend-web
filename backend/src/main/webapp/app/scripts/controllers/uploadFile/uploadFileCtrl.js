angular.module('sbAdminApp').controller('UploadFileCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, FileUploader, loadData) {
	console.log(loadData);
	console.log('UploadFileCtrl');
	var uploader;
	var askDetail;
	
	$scope.maxSize = 5;
	$scope.itemsPerPage = 10;
	$scope.totalItems = loadData.totalItems;
	$scope.periodObj = loadData.lastPeriod;
	$scope.uploadData = {};
	
	//-------
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
	
	
	
	//--------
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
    };
    
    
    
});