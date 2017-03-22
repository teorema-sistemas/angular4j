(function ()
{
    'use strict';

    angular
        .module('appAngular')
        .config(routeConfig);

    function routeConfig($stateProvider, $urlRouterProvider, $locationProvider)
    {
        $locationProvider.html5Mode(false);
        
        $urlRouterProvider.otherwise('/login');

        $stateProvider
        .state('app', {
            abstract: true,
            views   : {
                'main@'         : {
                    templateUrl: "app/layout/simple-page.html",
                    controller : 'MainController as vm'
                },
                'navbar@app'   : {
                    templateUrl: "app/main/navbar/navbar.html",
                }
            }
        });
    }

})();
