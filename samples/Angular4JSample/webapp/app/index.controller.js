(function ()
{
    'use strict';

    angular
        .module('appAngular')
        .controller('IndexController', IndexController);

    function IndexController($rootScope, $state)
    {
        var vm = this;
        
        $rootScope.authentication = {};
        $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams) {
        	if(toState.authenticate && !$rootScope.user){
                $state.transitionTo("app.login");
                event.preventDefault();
            }
        });
    }
})();