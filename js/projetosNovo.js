(function () {
	'use strict';

	angular.module('projetosAplicacao').controller('ProjetosNovoControle', function ($scope, $http) {
		$scope.projeto = {};

		$scope.criar = function () {
			$scope.sucesso = false;
			$scope.erroDoCliente400 = false;
			$scope.erroDoCliente409 = false;
			$scope.erroDoServidor = false;
			$scope.erroDeResposta = false;
			var requisicao = $http({
				method: 'PUT',
				url: '/projeto/' + $scope.projeto.nome
			});
			requisicao.success(function (dados, estado, cabecalhos, configuracoes) {
				$scope.sucesso = true;
				$scope.projeto.uri = cabecalhos('location').replace("/projeto/", "/#/projeto/");
			});
			requisicao.error(function (dados, estado, cabecalhos, configuracoes) {
				if (estado === 409) {
					$scope.erroDoCliente409 = true;
				} else if (estado === 400) {
					$scope.erroDoCliente400 = true;
				} else if (estado >= 500 && estado < 600) {
					$scope.erroDoServidor = true;
				} else {
					$scope.erroDeResposta = true;
				}
			});
		}
	});

}());