package com.bonitasoft.custompage.containership;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.log.event.BEventFactory;
import org.junit.Test;

import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantInformation;
import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantParameters;
import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantResult;
import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantState;
import com.bonitasoft.engine.platform.Tenant;

public class JUnitContainerShip {

    static Logger logger = Logger.getLogger(JUnitContainerShip.class.getName());

    @Test
    public void getListTenants() {
        // final APISession session = login();
        final TenantParameters tenantParameters = new TenantParameters();
        tenantParameters.serverUrl = "http://localhost:8080";
        tenantParameters.applicationName = "bonita";
        tenantParameters.isExternalCall = true;

        // <BONITA>/engine-server/work/platform/bonita-platform-community.properties
        tenantParameters.platformUsername = "platformAdmin";
        tenantParameters.platformPassword = "platform";
        final TenantResult tenantResult = ContainerShipAccess.getListTenants(tenantParameters);
        System.out.println("TenantResult = " + tenantResult.getAnswer());
    }

    @Test
    public void addAndRemoveTenants() {
        // final APISession session = login();
        final TenantParameters tenantParameters = new TenantParameters();
        tenantParameters.serverUrl = "http://localhost:8080";
        tenantParameters.applicationName = "bonita";
        tenantParameters.isExternalCall = true;

        // <BONITA>/engine-server/work/platform/bonita-platform-community.properties
        tenantParameters.platformUsername = "platformAdmin";
        tenantParameters.platformPassword = "platform";
        TenantResult tenantResult = ContainerShipAccess.addTenant(tenantParameters);
        // we should have an error here
        if (!BEventFactory.isError(tenantResult.listEvents)) {
            fail("Can't detect the error");
        }
        // now, create a correct tenant
        final String tenantName = "tenantTest3";
        tenantParameters.tenantName = tenantName;
        tenantParameters.tenantDescription = "TenantDescription";
        tenantParameters.tenantUsername = "newinstall";
        tenantParameters.tenantPassword = "install";
        tenantParameters.tenantActivate = true;
        tenantParameters.defaultBonitaHome = "C:/atelier/BPM-SP-7.1.0/workspace/tomcat/bonita/";

        // if the tenant alreay exist, start by remove it
        final TenantInformation tenantInformation = ContainerShipAccess.getTenantByName(tenantParameters, tenantName);
        if (tenantInformation.tenantState != ContainerShipAccess.TenantState.NOTEXIST)
        {
            tenantParameters.tenantId = tenantInformation.tenant.getId();
            tenantResult = ContainerShipAccess.removeTenant(tenantParameters);
        }

        tenantResult = ContainerShipAccess.addTenant(tenantParameters);
        if (BEventFactory.isError(tenantResult.listEvents)) {
            fail("Error at creation");
        }
        if (ContainerShipAccess.getTenantByName(tenantParameters, tenantName).tenantState != TenantState.ACTIVATED) {
            System.out.print("No correct, ");
            // desactivate it
        }

        // now remove the new tenant
        tenantParameters.tenantId = tenantResult.tenantId;
        tenantResult = ContainerShipAccess.desactivateTenant(tenantParameters);
        if (ContainerShipAccess.getTenantByName(tenantParameters, tenantName).tenantState != TenantState.DEACTIVATED) {
            System.out.print("No correct, ");
            // desactivate it
        }

        tenantResult = ContainerShipAccess.activateTenant(tenantParameters);
        if (ContainerShipAccess.getTenantByName(tenantParameters, tenantName).tenantState != TenantState.ACTIVATED) {
            System.out.print("No correct, ");
            // desactivate it
        }

        tenantResult = ContainerShipAccess.removeTenant(tenantParameters);
        if (BEventFactory.isError(tenantResult.listEvents)) {
            fail("Error at deletion");
        }
        if (!(ContainerShipAccess.getTenantByName(tenantParameters, tenantName).tenantState == TenantState.NOTEXIST)) {
            System.out.print("Tenant still present, ");
            // desactivate it
        }

        System.out.println("TenantResult = " + tenantResult.getAnswer());
    }

    @Test
    public void activateAllTenants()
    {
        final TenantParameters tenantParameters = new TenantParameters();
        tenantParameters.serverUrl = "http://localhost:8080";
        tenantParameters.applicationName = "bonita";
        tenantParameters.isExternalCall = true;

        // <BONITA>/engine-server/work/platform/bonita-platform-community.properties
        tenantParameters.platformUsername = "platformAdmin";
        tenantParameters.platformPassword = "platform";
        final TenantResult tenantResult = ContainerShipAccess.getListTenants(tenantParameters);

        for (final Tenant tenant : tenantResult.listTenants)
        {
            tenantParameters.tenantId = tenant.getId();

            final TenantResult tenantResultActivate = ContainerShipAccess.activateTenant(tenantParameters);
            System.out.println("Activate tenantId[" + tenant.getId() + "] : TenantResult = " + tenantResultActivate.getAnswer());

        }

    }
    @Test
    public void collectInformation()
    {
            // final APISession session = login();
            final TenantParameters tenantParameters = new TenantParameters();
            tenantParameters.serverUrl = "http://localhost:8080";
            tenantParameters.applicationName = "bonita";
        tenantParameters.isExternalCall = true;

            // <BONITA>/engine-server/work/platform/bonita-platform-community.properties
            tenantParameters.platformUsername = "platformAdmin";
            tenantParameters.platformPassword = "platform";
            final TenantResult tenantResult = ContainerShipAccess.getPlatformInformation(tenantParameters);
        System.out.println("PlatformInformation " + tenantResult.getAnswer().toString());

    }
    /**
     * @param tenantParameters
     * @param tenantName
     * @param tenantActivation
     * @return
     */



    public APISession login()
    {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("server.url", "http://localhost:8080");
        map.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, map);

        // Set the username and password
        // final String username = "helen.kelly";
        final String username = "walter.bates";
        final String password = "bpm";

        // get the LoginAPI using the TenantAPIAccessor
        LoginAPI loginAPI;
        try {
            loginAPI = TenantAPIAccessor.getLoginAPI();
            // log in to the tenant to create a session
            final APISession session = loginAPI.login(username, password);
            return session;
        } catch (final Exception e)
        {
            logger.severe("during login " + e.toString());
        }
        return null;
    }
}
