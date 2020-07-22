angular.module('sbAdminApp').controller('DealerCtrl', function($rootScope, $scope, $http, $state, $translate, urlPrefix, loadData) {	
	console.log('Dealer');
	
	$scope.dealers = loadData.dealers;
	$scope.dealer = {enabled: true};
	var myModal;
	
	$scope.persist = function() {
		$http.post(urlPrefix + '/restAct/dealer/persistDealer', {dealer: $scope.dealer}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.dealers = result.dealers;
			$scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.remove = function(id) {
		var confirm = window.confirm("Are you sure to remove ?");
		if (confirm != true) {
			return;
		}
		
		var obj = {id: id};
		
		$http.post(urlPrefix + '/restAct/dealer/remove', {dealer: obj}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.dealers = result.dealers;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	

	$scope.addEdit = function(dealer) {
		if(dealer) {
			$scope.dealer = dealer;
		} else {
			$scope.dealer = {enabled: true};
		}
		
		if(!myModal) {
			myModal = $('#myModal').modal();		
			myModal.on('shown.bs.modal', function (e) {
				//
			});
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
	}
	
	$scope.dismissModal = function() {
		if(!myModal) return;
		
		isDismissModal = true;
		myModal.modal('hide');
	}
	
});
