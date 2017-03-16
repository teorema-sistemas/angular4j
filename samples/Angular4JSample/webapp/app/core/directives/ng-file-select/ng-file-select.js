(function ()
{
	'use strict';
	
	angular
		.module('app.core')
		.directive("ngFileSelect",function(){
			return {
				link: function($scope,el, attrs){
					var key = attrs.ngFileSelect;
					el.bind("change", function(e){
						$scope.file = (e.srcElement || e.target).files[0];						
						$scope.getFile($scope.file, key);
					})
				}
			}
		});
})();