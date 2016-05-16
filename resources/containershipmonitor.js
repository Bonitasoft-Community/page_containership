'use strict';
/**
 *
 */

(function() {


var appCommand = angular.module('containershipmonitor', ['googlechart', 'ui.bootstrap', 'ngSanitize']);






// --------------------------------------------------------------------------
//
// Controler Ping
//
// --------------------------------------------------------------------------

// Ping the server
appCommand.controller('ContainerShipControler',
	function ( $http, $scope,$sce ) {

	this.showconnection=true;
	this.showinformation=false;
	
	this.isshowhistory=false;
	this.showhistory = function( showhistory ) {
		this.isshowhistory  = showhistory;
	};
	
	this.message="";
	this.platformUsername="platformAdmin";
	this.platformPassword = "platform";
	
	this.errormessage="";
	this.platforminfo= {};
	this.listtenants= [];
	this.tenantinformation={};
	this.listevents=[];
	this.listeventshtml="";
	
	

	this.connection = function()
	{
		this.connectionmessage="Processing...";
		this.connectionerrormessage="";
		var param = { "platformUsername": this.platformUsername,
					  "platformPassword": this.platformPassword };
					  
		var json= angular.toJson(param, param);
		var self=this;
		$http.get( '?page=custompage_containership&action=connection&jsonparam='+json  )
		.then( function (jsonResult) {
			self.connectionmessage="";
			self.connectionerrormessage ="";
			self.platforminfo 		= jsonResult.data.platforminfo;
			self.listtenants 		= jsonResult.data.listtenants;
			self.connectionlisteventshtml = jsonResult.data.listeventshtml;

	
		}, function (jsonResult) {
			this.connectionmessage="";
			self.connectionerrormessage = "Error from server : "+jsonResult.status;
			alert("Error from server : "+jsonResult.status);
		} );

	}

	
	this.getPlatformInformation = function () {
		alert("getPlatformInformation: start");

		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword };
		var json= angular.toJson(param, param);
		
		var self = this;
		self.loading = true;
		
		$http.get( '?page=custompage_containership&action=getplatforminformation&jsonparam='+json )
		      .then( function (jsonResult) {
						alert("getPlatformInformation: success");
						 /*self.platforminfo 		= response.data; */ 
						self.listevents 		= jsonResult.data.listevents;
						self.listeventshtml		= jsonResult.data.listeventshtml;
		      		}, 
		      		function (jsonResult) {
		      			self.errormessage = "Error from server : "+jsonResult.status;
		      			alert("getPlatformInformation:Error from server : "+jsonResult.status);
		      		} 
		      		).finally(function() {
		    			self.loading = false;
		    		});
		 
	};
	
	this.getListTenants = function () 	{
		var self = this;
		this.message="Processing...";
		this.errormessage="";
		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword };
		var json= angular.toJson(param, param);

		
		$http.get( '?page=custompage_containership&action=getlisttenants&jsonparam='+json )
		.then( function successCallback(response) {
			self.message="";
			self.listtenants = response.data;
		}, function errorCallback(response) {
			self.message="";
			self.errormessage = "Error from server : "+response.status;
			alert("getListTenants:Error from server : "+response.status);
		} );
	};
	
	
	/**
	 * refresh
	 */
	this.refresh = function() {
		this.getPlatformInformation();
		this.getListTenants();
	};
	
	this.getClassLine = function( tenantinfo ) {
		if (tenantinfo.state == "ACTIVATED")
			return "background-color: #18bc9c";
		if (tenantinfo.state == "DEACTIVATED")
			return "background-color: #f39c12;";
		return "";
			
	}
	/** ---------------------------------------------------------------------
	 * operations
	 */
	this.addTenant = function () {
		var self = this;
		this.message="Processing...";
		this.errormessage="";
		this.connectionlisteventshtml="";
		
		this.tenantinformation.platformUsername= this.platformUsername;
		this.tenantinformation.platformPassword= this.platformPassword;
		
		var json= angular.toJson(this.tenantinformation, true);
		
		$http.get( '?page=custompage_containership&action=addtenant&jsonparam='+json  )
		.then( function (jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
			
		}, function (jsonResult) {
			self.message="";
			self.errormessage = "Error from server : "+jsonResult.status;
			alert("Error from server : "+jsonResult.status);
		} );
	}
	
	
	

	this.activatetenant = function ( tenant ) {
		var self = this;
		this.message="Processing...";
		this.errormessage="";
		
		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "tenantId" : tenant.id };
		var json= angular.toJson(param, param);
		
		$http.get( '?page=custompage_containership&action=activatetenant&jsonparam='+json )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			alert("Error from server : "+jsonResult.status);
		} );
	}
	
	
	this.deactivatetenant = function ( tenant ) {
		var self = this;
		this.message="Processing...";
		this.connectionlisteventshtml="";

		this.errormessage="";
		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "tenantId" : tenant.id };
		var json= angular.toJson(param, param);

	
		$http.get( '?page=custompage_containership&action=desactivateTenant&jsonparam='+json  )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			alert("Error from server : "+jsonResult.status);
		} );
	}

	this.removetenant = function ( tenant ) {
		var self = this;
		this.message="Processing...";
		this.errormessage="";
		this.connectionlisteventshtml="";

		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "tenantId" : tenant.id };
		var json= angular.toJson(param, param);

		$http.get( '?page=custompage_containership&action=removetenant&jsonparam='+json  )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			alert("Error from server : "+jsonResult.status);
		} );
	}
	
	this.getListEvents = function ( listevents ) {
		return $sce.trustAsHtml(  listevents);
	}
	
});



})();