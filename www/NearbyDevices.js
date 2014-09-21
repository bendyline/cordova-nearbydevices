var nearbyDevices =  {
    startVirtualBeacon: function(uuid, majorNumber, minorNumber, successCallback, errorCallback) 
	{
        cordova.exec(
            successCallback, 
            errorCallback, 
            'NearbyDevices', 
            'startVirtualBeacon', 
            [uuid, majorNumber, minorNumber]
        );
    },
	listenForBeacons: function(successCallback, errorCallback) 
	{
        cordova.exec(
            successCallback, 
            errorCallback, 
            'NearbyDevices', 
            'listenForBeacons', 
            []
        );
    }
}
module.exports = nearbyDevices;

   var listenerService;

   document.addEventListener('deviceready', function() {
      var serviceName = 'com.Bendyline.NearbyDevices.ListenerBackgroundService';
      var factory = cordova.require('com.red_folder.phonegap.plugin.backgroundservice.BackgroundService')
      listenerService = factory.create(serviceName);

       go();
	}, true);

   function getStatus() {
      listenerService.getStatus(function(r){displayResult(r)}, function(e){displayError(e)});
   }

   function displayResult(data) {
      alert("Is service running: " + data.ServiceRunning);
   }

   function displayError(data) {
      alert("We have an error");
   }
   
   function go() {
   listenerService.getStatus(function(r){startService(r)}, function(e){displayError(e)});
};

function startService(data) {
   if (data.ServiceRunning) {
  //    alert("Service running");
      enableTimer(data);
   } else {
    //     alert("Starting service");

      listenerService.startService(function(r){enableTimer(r)}, function(e){displayError(e)});
   }
}

function enableTimer(data) {
   if (data.TimerEnabled) {
      registerForUpdates(data);
   } else {
        alert("Enabling timer");

      listenerService.enableTimer(5000, function(r){registerForUpdates(r)}, function(e){displayError(e)});
   }
}

function registerForUpdates(data) {
   if (!data.RegisteredForUpdates) {
    //     alert("Regging for updates");

      listenerService.registerForUpdates(function(r){updateHandler(r)}, function(e){handleError(e)});
   }
}
