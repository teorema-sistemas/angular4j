(function ()
{
    'use strict';

    angular
        .module('app.home')
        .controller('HomeController', HomeController);

    function HomeController($scope, $rootScope, homeModelView, fileReader){

    	var vm = this;
        vm.removeImage = removeImage;
        vm.restore = restore;
        vm.submit = submit;
        vm.reload = reload;
        vm.downloadImage = downloadImage;
        $scope.getFile = getFile;

    	vm.info = {};

        init();

        function init(){
            homeModelView.getImageAngular4j().then(function(response){
                vm.imageAngular4j = response;
            });

            $scope.$watch("vm.date", function(response){
                var m = moment(response);
                if(m.isValid()){
                    vm.info.date = new Date(m);
                }
            })

            homeModelView.getInfo().then(function(response){
                loadInfo(response);
            });
        }

        function restore(){
            homeModelView.restore().then(function(response){
                loadInfo(response);
            });
        }

        function submit(){
            homeModelView.submit(vm.info);
        }

        function reload(){
            homeModelView.reload().then(function(response){
                loadInfo(response);
            });

        }

        function removeImage(){
            delete vm.info.image;
        }

        function loadInfo(info){
            vm.info = info;
            console.log(info);
            var m = moment(vm.info.date);
            if(m.isValid()){
                vm.date = m.format("DD/MM/YYYY HH:mm:ss");
            }
        }

        function getFile(file, key) {         
            fileReader.readAsDataUrl(file, $scope)
            .then(function(result) {
                vm.info.image = result;
            });
        };

        function downloadImage(){
            homeModelView.downloadImage().then(function(response){
                var link = document.createElement("a");
                link.target = "_blank";
                link.href = response;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            });
        }
    }
})();