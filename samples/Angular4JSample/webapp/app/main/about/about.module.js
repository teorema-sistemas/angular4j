(function ()
{
    'use strict';

    angular
        .module('app.about', [])
        .config(config);

    function config($stateProvider){
    	$stateProvider
	    	.state('app.about', {
	            url: '/about',
	            authenticate: true,
	            views : {
	            	"content@app" : {
	            		templateUrl: 'app/main/about/about.html',
	            		controller: "AboutController"
	            	}
	            }
	        })
    }
})();