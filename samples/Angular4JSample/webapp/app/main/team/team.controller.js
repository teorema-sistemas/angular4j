(function ()
{
    'use strict';

    angular
        .module('app.team')
        .controller('TeamController', TeamController);

    function TeamController($scope, $rootScope, teamModelView){
    	var vm = this;

        init();

        function init(){
            teamModelView.getTeam().then(function(response){
                vm.team = response;
            });
        }
    }
})();