(function ()
{
    'use strict';

    angular
        .module('appAngular', [
            'ng4J',

            'ui.router',
            'ngAnimate',
            'ngSanitize',
            'ui.mask',
        
            'app.core',
            
            'app.home',
            'app.team',
            'app.about',
            'app.login'
        ]);
})();