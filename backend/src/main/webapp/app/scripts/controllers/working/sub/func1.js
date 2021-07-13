class Func1 {
	
	constructor(scope) {
	    this.scope = scope;
	}
	
	process() {
		var $scope = this.scope;
		console.log('Init');
		
		var isDismissModalAsk;
		var myModalAsk;
		$scope.addOtherTraceObj = {};
		$scope.addOtherTraceObj.addOtherTrace = function(obj) {
			console.log(obj);
			
			if(!myModalAsk) {
				myModalAsk = $('#myModal_other').modal();
				$(myModalAsk).draggable({scroll: false});
				
				myModalAsk.on('hide.bs.modal', function (e) {
					if(!isDismissModalAsk) {
						return e.preventDefault();
					}
					isDismissModalAsk = false;
				});
				myModalAsk.on('hidden.bs.modal', function (e) {
					//
				});
			} else {
				myModalAsk.modal('show');
			}
			
			$scope.addOtherTraceObj.addOtherTraceTitle = obj.name;
		}
		
		$scope.addOtherTraceObj.dismissModalAskOther = function() {
			$scope.disNotice = false;
			isDismissModalAsk = true;
			myModalAsk.modal('hide');
		}
		
		
		
	}
	
}