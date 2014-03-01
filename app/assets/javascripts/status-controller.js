var statusController = angular.module('StatusController', []);

function mainController($scope, $http) {
    var startToken = "start";
    var stopToken = "stop";
    var startingToken = "starting";
    var stoppingToken = "stopping";

    var crawlStateChange = "CrawlStateChanged";
    var crawlDir = "CrawlDir";

    var maxDisplayDirs = 10;

	$scope.crawlState = "retrieving";
	$scope.dirs = [];
	$scope.crawlButtonText = "pending";

    // TODO: how do you recover from connection issues?
	function doWebSocket() {
        var wsUrl = jsRoutes.controllers.Application.crawlerStatusWs().webSocketURL()

        $scope.socket = new WebSocket(wsUrl)

        $scope.socket.onmessage =  function (msg) {
          $scope.$apply( function() {
                var event = JSON.parse(msg.data);
                if (event.eventType === crawlStateChange) {
                    $scope.crawlState = event.crawlState;
                } else if (event.eventType === crawlDir) {
                    console.log("got crawl dir " + event.dir)
                    var len = $scope.dirs.length
                    if (len >= maxDisplayDirs) {
                        $scope.dirs.splice(0, 1 + len - maxDisplayDirs);
                    }
                    $scope.dirs.push(event.dir);
                }
            })
          }
	}

	$http.get(jsRoutes.controllers.Application.crawlerStatus().url)
		.success(function(data) {
			$scope.crawlState = data.crawlState;
			if(data.crawlState == "Started") {
			    $scope.crawlButtonText = stopToken;
			} else if (data.crawlState == "Stopped") {
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
