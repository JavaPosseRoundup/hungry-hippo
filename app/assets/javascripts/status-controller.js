var statusController = angular.module('StatusController', []);

function mainController($scope, $http) {

	$scope.status = "retrieving";

	function doWebSocket() {
        var wsUrl = jsRoutes.controllers.Application.crawlerStatusWs().webSocketURL()

        $scope.socket = new WebSocket(wsUrl)

        $scope.socket.onmessage =  function (msg) {
          $scope.$apply( function() {
                console.log("received : #{msg}");
                $scope.status = msg.data;
            })
          }
	}

	$http.get(jsRoutes.controllers.Application.crawlerStatus().url)
		.success(function(data) {
			$scope.status = data;
			doWebSocket();
		})
		.error(function(data) {
		    doWebSocket();
		});
}

