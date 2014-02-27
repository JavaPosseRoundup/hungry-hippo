var statusController = angular.module('StatusController', []);

function mainController($scope, $http) {
	$scope.status = "unknown";

    var wsUrl = jsRoutes.controllers.Application.crawlerStatusWs().webSocketURL()

    $scope.socket = new WebSocket(wsUrl)

    $scope.socket.onmessage =  function (msg) {
      $scope.$apply( function() {
            console.log("received : #{msg}");
            $scope.status = msg.data;
        })
      }
}