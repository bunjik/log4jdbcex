'use strict';

var app = angular.module("logger-ui", ["ui.bootstrap", "cfp.hotkeys"]);

app.config(function(hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = true;
});

app.controller("appCtrl", function($scope, $http, $q, $interval, hotkeys) {

	$scope.activeTab = { history: true, running: false, setting: false };
	$scope.historyDsTab = {};
	$scope.runningDsTab = {};
	$scope.settingDsTab = {};
	$scope.allQueriesTab = "[ALL]";

	$scope.historyMap = {};
	$scope.runningMap = {};
	$scope.settings = {};
	$scope.servers = "";
	$scope.isLoading = false;

	$scope.historyLastUpdate;
	$scope.runningLastUpdate;
	$scope.historyAutoLoading = false;
	$scope.runningAutoLoading = false;
	$scope.historyTimerPromise = null;
	$scope.runningTimerPromise = null;

	// デフォルトのホットキー設定
	hotkeys.add({
		combo: 'q',
		description: 'Select Query History Tab.',
		callback: function() { $scope.selectTab("history"); }
	});
	hotkeys.add({
		combo: 'w',
		description: 'Select Running Queries Tab.',
		callback: function() { $scope.selectTab("running"); }
	});
	hotkeys.add({
		combo: 'e',
		description: 'Select Settings Tab.',
		callback: function() { $scope.selectTab("setting"); }
	});
	hotkeys.add({
		combo: 'r',
		description: 'Reload.',
		callback: function() {
			if ($scope.activeTab.history) $scope.loadHistory();
			if ($scope.activeTab.running) $scope.loadRunning();
			if ($scope.activeTab.setting) $scope.loadSetting();
		}
	});
	hotkeys.add({
		combo: 't',
		description: 'toggle Auto Reload.',
		callback: function() { $scope.toggleAutoReload(); }
	});

	/**
	 **********************************
	 * 初期化処理
	 **********************************
	 */
	$scope.init = function() {
		// load servers
		$scope.servers = localStorage.getItem("servers");

		var promiseList = [];
		promiseList.push($scope.loadHistory());
		promiseList.push($scope.loadRunning());
		promiseList.push($scope.loadSetting());
		Promise.all(promiseList).then(function() {
			// datasource毎のhotkey設定
			var idx = 1;
			angular.forEach($scope.historyMap, function(elem, name) {
				$scope.historyDsTab[name] = (idx == 1);
				$scope.runningDsTab[name] = (idx == 1);
				$scope.settingDsTab[name] = (idx == 1);
				hotkeys.add({
					combo: "" + idx,
					description: "Select " + name + " Tab.",
					callback: function() { $scope.selectDsTab(name); }
				});
				idx++;
			});
		});
	}

	/**
	 **********************************
	 **********************************
	 */
	$scope.selectTab = function(tab) {
		angular.forEach($scope.activeTab, function(val, key) {
			$scope.activeTab[key] = (key == tab);
		});
	}

	/**
	 **********************************
	 **********************************
	 */
	$scope.selectDsTab = function(tab) {
		var tabList;
		if ($scope.activeTab['history']) {
			tabList = $scope.historyDsTab;
		} else if ($scope.activeTab['running']) {
			tabList = $scope.runningDsTab;
		} else if ($scope.activeTab['setting'] && tab != $scope.allQueriesTab) {
			tabList = $scope.settingDsTab;
		} else {
			return;
		}

		angular.forEach(tabList, function(val, key) {
			tabList[key] = (key == tab);
		});
	}

	/**
	 **********************************
	 **********************************
	 */
	$scope.toggleAutoReload = function() {
		if ($scope.activeTab['history']) {
			$scope.historyAutoLoading = !$scope.historyAutoLoading;
			if ($scope.historyAutoLoading) {
				$scope.historyTimerPromise = $interval(function() { $scope.loadHistory() }, 10000);
				$scope.loadHistory();
			} else {
				$interval.cancel($scope.historyTimerPromise);
			}
		} else if ($scope.activeTab['running']) {
			$scope.runningAutoLoading = !$scope.runningAutoLoading;
			if ($scope.runningAutoLoading) {
				$scope.runningTimerPromise = $interval(function() { $scope.loadRunning() }, 10000);
				$scope.loadRunning();
			} else {
				$interval.cancel($scope.runningTimerPromise);
			}
		}
	}

	/**
	 **********************************
	 * 実行済SQLのロード
	 **********************************
	 */
	$scope.loadHistory = function() {
		var defer = $q.defer();
		sendRequest("../history").then(function(res) {
			res[$scope.allQueriesTab] = Object.keys(res)
						.reduce(function(prev, cur) { return prev.concat(res[cur]) }, [])
						.sort(function(a, b) { return b.time - a.time });
			$scope.historyMap = res;
			return defer.resolve();
		}).finally(function() {
			$scope.historyLastUpdate = new Date();
		});
		return defer.promise;
	}

	/**
	 **********************************
	 * 実行中SQLのロード
	 **********************************
	 */
	$scope.loadRunning = function() {
		var defer = $q.defer();
		sendRequest("../running").then(function(res) {
			// 全DSの結果をマージしてソートする
			res[$scope.allQueriesTab] = Object.keys(res)
				.reduce(function(prev, cur) { return prev.concat(res[cur]) }, [])
				.sort(function(a, b) { return b.time - a.time });
			$scope.runningMap = res;
			return defer.resolve();
		}).finally(function() {
			$scope.runningLastUpdate = new Date();
		});
		return defer.promise;
	}

	/**
	 **********************************
	 * 設定情報のロード
	 **********************************
	 */
	$scope.loadSetting = function() {
		var defer = $q.defer();
		sendRequest("../setting").then(function(res) {
			$scope.settings = res;
			return defer.resolve();
		});
		return defer.promise;
	}

	/**
	 **********************************
	 * 設定情報のセーブ
	 **********************************
	 */
	$scope.saveSetting = function() {
		$scope.isLoading = true;
		$http({
			method: "PUT",
			url: "../setting",
			params: {
				servers: $scope.servers,
			},
			data: $scope.settings,
			cache: false
		}).then(function(res) {
			// 設定後の結果を再設定
			$scope.settings = res.data;
		}).finally(function() {
			$scope.isLoading = false;
		});
	}

	/**
	 **********************************
	 **********************************
	 */
	$scope.saveServers = function(servers) {
		// save servers
		if (servers == null) servers = "";
		localStorage.setItem("servers", servers);
		//console.log("save servers. [" + servers + "]");
	}

	/**
	 **********************************
	 **********************************
	 */
	function sendRequest(url) {
		$scope.isLoading = true;
		return $http({
			method: "GET",
			url: url,
			params: {
				servers: $scope.servers,
			},
			cache: false
		}).then(function(res) {
			return res.data;
		}).finally(function() {
			$scope.isLoading = false;
		});
	}
});

