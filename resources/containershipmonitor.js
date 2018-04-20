'use strict';
/**
 *
 */

(function() {


var appCommand = angular.module('containershipmonitor', ['googlechart', 'ui.bootstrap', 'ngSanitize','ngModal']);






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
	
	this.isConnected=false;
	
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
		var self=this;
		self.inprogress=true;
		self.connectionmessage="Processing...";
		self.connectionerrormessage="";
		var param = { "platformUsername": this.platformUsername,
					  "platformPassword": this.platformPassword };
					  
		var json= encodeURI(angular.toJson(param, param));
		
		$http.get( '?page=custompage_containership&action=connection&paramjson='+json  )
		.then( function (jsonResult) {
			self.connectionmessage="";
			self.connectionerrormessage ="";
			self.platforminfo 		= jsonResult.data.platforminfo;
			self.listtenants 		= jsonResult.data.listtenants;
			self.connectionlisteventshtml = jsonResult.data.listeventshtml;
			self.isConnected=true;
			self.inprogress=false;
	
		}, function (jsonResult) {
			this.connectionmessage="";
			self.connectionerrormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
		} );

	}

	
	this.getPlatformInformation = function () {
		// alert("getPlatformInformation: start");

		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword };
		var json= encodeURI( angular.toJson(param, param) );
		
		var self = this;
		self.inprogress=true;
		self.loading = true;
		
		$http.get( '?page=custompage_containership&action=getplatforminformation&paramjson='+json )
		      .then( function (jsonResult) {
						// alert("getPlatformInformation: success");
						 /*self.platforminfo 		= response.data; */ 
						self.listevents 		= jsonResult.data.listevents;
						self.listeventshtml		= jsonResult.data.listeventshtml;
						self.inprogress=false;
		      		}, 
		      		function (jsonResult) {
		      			self.errormessage = "Error from server : "+jsonResult.status;
		      			self.inprogress=false;
		      		} 
		      		).finally(function() {
		    			self.loading = false;
		    		});
		 
	};
	
	
	// ----------------------------- get list tenants 
	this.getListTenants = function () 	{
		var self = this;
		self.inprogress=true;
		this.message="Processing...";
		this.errormessage="";
		this.listeventshtml='';

		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword };
		var json= encodeURI( angular.toJson(param, param));

		
		$http.get( '?page=custompage_containership&action=getlisttenants&paramjson='+json )
		.then( function successCallback(response) {
			self.message="";
			self.listtenants = response.data.listtenants;
			self.inprogress=false;
		}, function errorCallback(response) {
			self.message="";
			self.errormessage = "Error from server : "+response.status;
			self.inprogress=false;
		} );
	};
	
	
	/**
	 * refresh
	 */
	this.refresh = function() {
		this.listeventshtml='';
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
		self.inprogress=true;
		this.message="Processing...";
		this.errormessage="";
		this.connectionlisteventshtml="";
		
		this.tenantinformation.platformUsername= this.platformUsername;
		this.tenantinformation.platformPassword= this.platformPassword;
		
		var json= encodeURI( angular.toJson(this.tenantinformation, true));
		
		$http.get( '?page=custompage_containership&action=addtenant&paramjson='+json  )
		.then( function (jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
			self.inprogress=false;
		}, function (jsonResult) {
			self.message="";
			self.errormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
		} );
	}
	
	
	this.editTenant = function () {
		var self = this;
		self.inprogress=true;
		this.message="Processing...";
		this.errormessage="";
		this.connectionlisteventshtml="";
		
		this.tenantinformation.platformUsername= this.platformUsername;
		this.tenantinformation.platformPassword= this.platformPassword;
		
		var json= encodeURI( angular.toJson(this.tenantinformation, true));
		
		$http.get( '?page=custompage_containership&action=edittenant&paramjson='+json  )
		.then( function (jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
			self.inprogress=false;
		}, function (jsonResult) {
			self.message="";
			self.errormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
		} );
	}
	
	
	
	

	this.activatetenant = function ( tenant ) {
		var self = this;
		self.inprogress=true;
		this.message="Processing...";
		this.errormessage="";
		
		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "id" : tenant.id };
		var json= encodeURI(angular.toJson(param, param));
		
		$http.get( '?page=custompage_containership&action=activatetenant&paramjson='+json )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
			self.inprogress=false;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
		} );
	}
	
	
	this.deactivatetenant = function ( tenant ) {
		var self = this;
		self.inprogress=true;
		this.message="Processing...";
		this.connectionlisteventshtml="";

		this.errormessage="";
		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "id" : tenant.id };
		var json= encodeURI(angular.toJson(param, param));

	
		$http.get( '?page=custompage_containership&action=desactivateTenant&paramjson='+json  )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;7
			self.inprogress=false;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
		} );
	}

	this.removetenant = function ( tenant ) {
		var self = this;
		this.message="Processing...";
		self.inprogress=true;
		this.errormessage="";
		this.connectionlisteventshtml="";

		var param = { "platformUsername": this.platformUsername,
				  "platformPassword": this.platformPassword,
				  "id" : tenant.id };
		var json= encodeURI(angular.toJson(param, param));

		$http.get( '?page=custompage_containership&action=removetenant&paramjson='+json  )
		.then( function successCallback(jsonResult) {
			self.message		= jsonResult.data.message;
			self.listtenants 	= jsonResult.data.listtenants;
			self.listeventshtml = jsonResult.data.listeventshtml;
			self.inprogress=false;
		}, function errorCallback(jsonResult) {
			self.errormessage = "Error from server : "+jsonResult.status;
			self.inprogress=false;
			
		} );
	}
	
	this.getListEvents = function ( listevents ) {
		return $sce.trustAsHtml(  listevents);
	}
	
	
	//  manage the modal 
	
	this.isshowTenantInformation=false;
	this.isAddTenant = false;
	this.openTenantInformation = function () {
		this.listeventshtml=''; 
		this.isshowTenantInformation=true;
		
		
	}
	this.closeTenantInformation = function () {
		this.isshowTenantInformation=false;
	}
	this.openToAddTenantInformation = function()
	{
		this.isAddTenant = true;
		this.isEditTenant = false;
		this.openTenantInformation();
	}
	
	this.openEditTenant = function( tenantinfo ) {
		this.isAddTenant =false;
		this.isEditTenant = true;
		
		this.tenantinformation = tenantinfo;
	// alert("Tenant Status ["+ angular.toJson( tenantinfo ) +"]");
		
		if (tenantinfo.state=="ACTIVATED")
			this.tenantinformation.tenantActivate=true;
		else
			this.tenantinformation.tenantActivate=false;
		
		
		this.openTenantInformation();
	}
});



})();