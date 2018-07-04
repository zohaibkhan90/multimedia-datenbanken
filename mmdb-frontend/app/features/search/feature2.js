'use strict';

angular
  .module('myApp.search', ['ui.router'])
  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
      .state('search', {
        url: "/search",
        templateUrl: "features/search/feature2.html",
        controller: 'Feature2Ctrl'
      })
    ;
  })
  .controller('Feature2Ctrl', function ($scope, $document, $timeout, httpInterface) {
    $scope.inputObject = $('input[name=userFile]');
    
    $scope.images = [];
    $scope.isFileSelected = false;
    $scope.fileName = "";
    $scope.selectedImage = undefined;
    
    $scope.inputObject.change(function(event) {
      var file = $scope.inputObject[0].files[0];
      var reader = new FileReader();
      
      reader.readAsDataURL(file);

      reader.onload = function () {
        var encodedImage = reader.result.split(',')[1];

        $('img[id=uploaded]')[0].setAttribute(
          'src', 'data:image/png;base64,' + encodedImage
        );

        // $('label[id=browseBtn]')[0].innerText = "Selected File: " + file.name;

        var scope = angular.element(document.getElementById('outer')).scope();
        scope.$apply(function(){
            scope.selectedImage = encodedImage;
            scope.fileName = file.name;
            scope.isFileSelected = true;
        });
      };
    });

    $scope.upload = function () {
      if($scope.selectedImage){
        httpInterface.getResults($scope.selectedImage).then(function(data) {
          $scope.images = data.data;

          if($scope.images && $scope.images.length == 12){
            angular.forEach($scope.images, function(value, key) {
              var selector = 'img[id=' + (key) + ']';
              $(selector)[0].setAttribute(
                'src', 'data:image/png;base64,' + value
              );
            });
          }
        });
      }
    }

    $scope.clear = function () {
      $scope.images = [];
      $scope.isFileSelected = false;
      $scope.fileName = "";
      $scope.selectedImage = undefined;
    }

  })
  .service('httpInterface', function($http, $httpParamSerializer) {
    this.getResults = function (uploadImage) {
      return $http({
        url: '/getSimilarityResults', 
        method: "POST",
        data: $httpParamSerializer({ encodedImage: uploadImage }),
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
      });
    }
  });
