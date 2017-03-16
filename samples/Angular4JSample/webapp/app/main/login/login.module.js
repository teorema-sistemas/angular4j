(function ()
{
    'use strict';

    angular
        .module('app.login', [])
        .config(config);

    function config($stateProvider){
    	$stateProvider
	        .state('app.login', {
	            url: '/login',
	            views : {
	            	"main@" : {
	            		templateUrl: 'app/main/login/login.html',
	            		controller: "LoginController as vm"
	            	}
	            }
	        })
    }
})();