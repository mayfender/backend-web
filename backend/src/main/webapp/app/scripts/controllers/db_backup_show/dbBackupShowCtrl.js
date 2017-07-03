angular.module('sbAdminApp').controller('DbBackupShowCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, FileUploader, urlPrefix, loadData) {
	
	$scope.dirList = loadData.dirList;
	$scope.fileList = loadData.fileList;
	$scope.dir = $scope.dirList[0];
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/setting/findDBBackup', {
			dir: $scope.dir 
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.fileList = result.fileList;	
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.download = function(fileName) {
		$http.get(urlPrefix + '/restAct/setting/downloadDBBack?dir=' + $scope.dir +'&fileName=' + fileName, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.deleteDb = function(fileName) {
		var isDelete = confirm('ยืนยันการลบ');
	    if(!isDelete) return;
	    
		$http.post(urlPrefix + '/restAct/setting/deleteDb', {
			dir: $scope.dir,
			fileName: fileName
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.fileList = result.fileList;	
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});