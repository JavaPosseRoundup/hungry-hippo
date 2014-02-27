var timerController = angular.module('TimerController', []);

function mainController($scope, $http) {
//	$scope.formData = {};

    var wsUrl = jsRoutes.controllers.Application.indexWS().webSocketURL()

    $scope.socket = new WebSocket(wsUrl)

    $scope.socket.onmessage( function (msg) {
      $scope.$apply( function() {
            console.log("received : #{msg}");
            $scope.time = JSON.parse(msg.data).data;
        })
      })

//	// when landing on the page, get all todos and show them
//	$http.get('/api/todos')
//		.success(function(data) {
//			$scope.todos = data;
//		})
//		.error(function(data) {
//			console.log('Error: ' + data);
//		});

    $scope.start = function() {
        $http.get(jsRoutes.controllers.Application.start().url).success();
    }

    $scope.stop = function() {
        $http.get(jsRoutes.controllers.Application.stop().url).success();
    }

//	// when submitting the add form, send the text to the node API
//	$scope.createTodo = function() {
//		$http.post('/api/todos', $scope.formData)
//			.success(function(data) {
//				$('input').val('');
//				$scope.todos = data;
//			})
//			.error(function(data) {
//				console.log('Error: ' + data);
//			});
//	};
//
//	// delete a todo after checking it
//	$scope.deleteTodo = function(id) {
//		$http.delete('/api/todos/' + id)
//			.success(function(data) {
//				$scope.todos = data;
//			})
//			.error(function(data) {
//				console.log('Error: ' + data);
//			});
//	};

}