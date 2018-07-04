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
    $scope.images = [];
    $scope.inputObject = $('input[name=userFile]');
    
    $scope.inputObject.change(function(event) {
      var file = $scope.inputObject[0].files[0];
      var reader = new FileReader();
      
      reader.readAsDataURL(file);

      reader.onload = function () {
        var encodedImage = reader.result.split(',')[1];

        $('img[id=uploaded]')[0].setAttribute(
          'src', 'data:image/png;base64,' + encodedImage
        );
        
        httpInterface.getResults(encodedImage).then(function(data) {
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
      };
    });

    $scope.clear = function () {
      $scope.images = [];
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
