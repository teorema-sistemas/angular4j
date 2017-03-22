(function ()
{
    'use strict';

    angular
        .module('app.team', [])
        .config(config);

    function config($stateProvider){
    	$stateProvider
	    	.state('app.team', {
	            url: '/team',
	            authenticate: true,
	            views : {
	            	"content@app" : {
	            		templateUrl: 'app/main/team/team.html',
	            		controller: "TeamController as vm"
	            	}
	            }
	        })
    }
})();