(function ()
{
    'use strict';

    angular
        .module('app.home', [])
        .config(config);

    function config($stateProvider){
    	$stateProvider
	    	.state('app.home', {
	            url: '/home',
	            authenticate: true,
	            views : {
	            	"content@app" : {
	            		templateUrl: 'app/main/home/home.html',
	            		controller: "HomeController as vm"
	            	}
	            }
	        })
    }
})();