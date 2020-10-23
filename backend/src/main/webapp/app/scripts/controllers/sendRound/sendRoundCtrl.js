angular.module('sbAdminApp').controller('SendRoundCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('SendRoundCtrl');
	
	$scope.sendRound = {
		addEditPanel: 0,
		fieldIndex: 0
	};
	$scope.sendRound.formData = {};
	$scope.sendRound.list = loadData.dataList;
	$scope.priceList = {};
	
	$scope.preventOuterEvent = function(e) {
		e.stopPropagation();
	}
	
	$scope.statusToggle = function(obj) {
		$http.post(urlPrefix + '/restAct/sendRound/statusToggle', {
			id: obj.id,
			enabled: obj.enabled,
			dealerId: $rootScope.workingOnDealer.id,
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			obj.enabled = obj.enabled ? false : true;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	$scope.sendRound.addEdit = function(panel, obj) {
		$scope.sendRound.addEditPanel = panel;
		$scope.sendRound.fieldIndex = 0;
		
		if($scope.sendRound.addEditPanel == 2) {
			$scope.sendRound.formData.sendRoundId = obj.id;
			$scope.sendRound.formData.name = obj.name;
			$("input[name='limitedTime']").data("DateTimePicker").date(new Date(obj.limitedTime));
		} else {
			initFields();
		}
	}
	
	$scope.sendRound.saveUpdate = function() {
		var periodDateObj = $("input[name='limitedTime']").data("DateTimePicker");
		var periodDate = periodDateObj && periodDateObj.date();
		
		if(periodDate == null) return;
		
		$scope.sendRound.formData.limitedTime = periodDate.toDate();
		$scope.sendRound.formData.limitedTime.setSeconds(59);
		
		$http.post(urlPrefix + '/restAct/sendRound/saveUpdate', {
			name: $scope.sendRound.formData.name,
			limitedTime: $scope.sendRound.formData.limitedTime,
			dealerId: $rootScope.workingOnDealer.id,
			id: $scope.sendRound.formData.sendRoundId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.sendRound.addEditPanel = 0;
			getDataList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	function getDataList() {
		$http.get(urlPrefix + '/restAct/sendRound/getDataList?dealerId=' + $rootScope.workingOnDealer.id).then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.sendRound.list = result.dataList;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function initDateEl() {		
		$('.dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'LT',
				locale: 'th'
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				
			});
		});
	}
	
	function initFields() {
		$scope.sendRound.formData.name = null;
		$scope.sendRound.formData.limitedTime = null;
		$scope.sendRound.formData.sendRoundId = null;
		
		var periodDateObj = $("input[name='limitedTime']").data("DateTimePicker");
		if(periodDateObj) {			
			periodDateObj.clear();
		}
	}
	
	initFields();
	initDateEl();
	
});