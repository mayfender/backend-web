class Func1 {
	
	constructor(scope) {
	    this.scope = scope;
	}
	
	process() {
		var $scope = this.scope;
		$scope.result = 'Initial...';
		
		//[.]
		$scope.$parent.addPopup = function() {
			console.log('addPopup');
			$('#mayfender').modal();
		}
		
		$scope.submit = function() {
			$scope.result = 'Yo ' + $scope.func;
		};
		
	}
	
}