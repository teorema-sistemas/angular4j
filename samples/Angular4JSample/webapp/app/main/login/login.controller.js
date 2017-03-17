(function ()
{
    'use strict';

    angular
        .module('app.login')
        .controller('LoginController', LoginController);

    function LoginController($scope, $rootScope, $state, loginModelView){
    	var vm = this;

    	vm.credentials = {};
    	vm.authenticate = authenticate;
    	vm.hasErrors = false;

    	function authenticate(){
    		loginModelView.authenticate(vm.credentials.email, vm.credentials.password).then(function(response){
    			if(Object.keys(response).length > 0){
    				$rootScope.user = response;
                    loginModelView.submit(response);
    				$state.go('app.home', {
                        reload : true
                    });
    			}else{
    				vm.hasErrors = true;
    			}
    		});
    	}
    }
})();