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
	function ( $http, $scope,$sce,$window ) {

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
		self.platformUsername =this.newplatformUsername;
		self.platformPassword =this.newplatformPassword;
		
		var param = { "platformUsername": self.platformUsername,
					  "platformPassword": self.platformPassword };
					  
		var json= encodeURI(angular.toJson(param, param));
		
		$http.get( '?page=custompage_containership&action=connection&paramjson='+json+"&t="+Date.now()  )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.inprogress=false;
				self.connectionmessage="";
				self.connectionerrormessage ="";
				self.jsonResult 		= jsonResult.platforminfo;
				self.listtenants 		= jsonResult.listtenants;
				self.connectionlisteventshtml = jsonResult.listeventshtml;
				self.isConnected=true;
			})
      		.error( function(jsonResult, statusHttp, headers, config) {
      			self.connectionmessage="";
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
		
		$http.get( '?page=custompage_containership&action=getplatforminformation&paramjson='+json+"&t="+Date.now() )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				// alert("getPlatformInformation: success");
				 /*self.platforminfo 		= response.data; */ 
				self.listevents 		= jsonResult.listevents;
				self.listeventshtml		= jsonResult.listeventshtml;
				self.inprogress=false;
    			self.loading = false;
      		})
      		.error( function(jsonResult, statusHttp, headers, config) {
	      			self.errormessage = "Error from server : "+jsonResult.status;
	      			self.inprogress=false;
	    			self.loading = false;
	      		} );
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

		
		$http.get( '?page=custompage_containership&action=getlisttenants&paramjson='+json+"&t="+Date.now() )
	      	.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.message="";
				self.listtenants = response.data.listtenants;
				self.inprogress=false;
		     })
      		.error( function(jsonResult, statusHttp, headers, config) {
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
		
		$http.get( '?page=custompage_containership&action=addtenant&paramjson='+json+"&t="+Date.now()  )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
			
				self.message		= jsonResult.message;
				self.listtenants 	= jsonResult.listtenants;
				self.listeventshtml = jsonResult.listeventshtml;
				self.inprogress=false;
			})
	      	.error( function(jsonResult, statusHttp, headers, config) {
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
		
		$http.get( '?page=custompage_containership&action=edittenant&paramjson='+json +"&t="+Date.now() )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.message		= jsonResult.message;
				self.listtenants 	= jsonResult.listtenants;
				self.listeventshtml = jsonResult.listeventshtml;
				self.inprogress=false;
			})
	      	.error( function(jsonResult, statusHttp, headers, config) {		
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
		
		$http.get( '?page=custompage_containership&action=activatetenant&paramjson='+json+"&t="+Date.now() )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.message		= jsonResult.message;
				self.listtenants 	= jsonResult.listtenants;
				self.listeventshtml = jsonResult.listeventshtml;
				self.inprogress=false;
			})
	      	.error( function(jsonResult, statusHttp, headers, config) {
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
		
	
		$http.get( '?page=custompage_containership&action=desactivateTenant&paramjson='+json+"&t="+Date.now() )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.message		= jsonResult.message;
				self.listtenants 	= jsonResult.listtenants;
				self.listeventshtml = jsonResult.listeventshtml;7
				self.inprogress=false;
			})
	      	.error( function(jsonResult, statusHttp, headers, config) {
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

		$http.get( '?page=custompage_containership&action=removetenant&paramjson='+json+"&t="+Date.now()  )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}
				self.message		= jsonResult.message;
				self.listtenants 	= jsonResult.listtenants;
				self.listeventshtml = jsonResult.listeventshtml;
				self.inprogress=false;
			})
	      	.error( function(jsonResult, statusHttp, headers, config) {
				self.errormessage = "Error from server : "+jsonResult.status;
				self.inprogress=false;
			
		} );
	}
	
	this.getListEvents = function ( listevents ) {
		return $sce.trustAsHtml(  listevents);
	}
	
	
	this.openTenantInAWindow = function( tenant ) {
		$window.open({"url": "/bonita?tenant="+tenant.id, "incognito": true})
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