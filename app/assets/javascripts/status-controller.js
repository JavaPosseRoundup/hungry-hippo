var statusController = angular.module('StatusController', []);

function mainController($scope, $http) {
    var startToken = "start";
    var stopToken = "stop";
    var startingToken = "starting";
    var stoppingToken = "stopping";

	$scope.status = "retrieving";
	$scope.crawlButtonText = "pending";

    // TODO: how do you recover from connection issues?
	function doWebSocket() {
        var wsUrl = jsRoutes.controllers.Application.crawlerStatusWs().webSocketURL()

        $scope.socket = new WebSocket(wsUrl)

        $scope.socket.onmessage =  function (msg) {
          $scope.$apply( function() {
                $scope.status = msg.data;
            })
          }
	}

	$http.get(jsRoutes.controllers.Application.crawlerStatus().url)
		.success(function(data) {
			$scope.status = data;
			if(data.state == "Started") {
			    $scope.crawlButtonText = stopToken;
			} else if (data.state == "Stopped") {
			    $scope.crawlButtonText = startToken;
			}
			doWebSocket();
		})
		.error(function(data) {
		    doWebSocket();
		});

    $scope.crawlCommand = function() {
        if ($scope.crawlButtonText == startToken)
            startCrawl();
        else if ($scope.crawlButtonText == stopToken)
            stopCrawl();
    }

    function startCrawl() {
        $scope.crawlButtonText = startingToken;
        $http.get(jsRoutes.controllers.Application.startCrawler().url)
            .success(function(data) {
                $scope.crawlButtonText = stopToken;
            })
//            .error(function(data) {
//                // TODO: try again
//            })
    }

    function stopCrawl() {
        $scope.crawlButtonText = stoppingToken;
        $http.get(jsRoutes.controllers.Application.stopCrawler().url)
            .success(function(data) {
                $scope.crawlButtonText = startToken;
            })
//            .error(function(data) {
//                // TODO: try again
//            })
    }
}

