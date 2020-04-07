package com.bonitasoft.custompage.containership;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;
import org.json.simple.JSONValue;

import com.bonitasoft.custompage.containership.Toolbox;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantActivationException;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.platform.TenantUpdater;

public class ContainerShipAccess {

    private static Logger logger = Logger.getLogger(ContainerShipAccess.class.getName());

    private static BEvent eventTenantAdminNotAvailable = new BEvent(ContainerShipAccess.class.getName(), 1, Level.APPLICATIONERROR,
            "Tenant Administration not available", "The tenant administration is not available in the community version", "Tenant can't be created", "Use a subscription server");

    private static BEvent eventPlatformLogin = new BEvent(ContainerShipAccess.class.getName(), 2, Level.APPLICATIONERROR,
            "Platform login error", "login password is incorrect for the PLATFORM connection", "Operation failed","Give a correct password");

    private static BEvent eventBadPlatform = new BEvent(ContainerShipAccess.class.getName(), 3, Level.CRITICAL,
            "Bad platform", "The platform is not correctly installed", "Operation failed", "Check server log");

    private static BEvent eventCantCreationTenant = new BEvent(ContainerShipAccess.class.getName(), 4, Level.APPLICATIONERROR,
            "Can't create the tenant", "With the information, it's not possible to create the tenant", "Tenant can't be created", "Give all required information" );

    private static BEvent eventCantDeleteTenant = new BEvent(ContainerShipAccess.class.getName(), 5, Level.APPLICATIONERROR,
            "Can't delete the tenant", "The deletion failed", "Operation can't be done", "Check the error");

    private static BEvent eventTenantCreationMissingParameter = new BEvent(ContainerShipAccess.class.getName(), 6, Level.APPLICATIONERROR,
            "Missing parameter to create the tenant", "A set of minimal parameters has to be give to create the tenant", "Operation can't be done", "Give all informations");

    private static BEvent eventCantActivateTenant = new BEvent(ContainerShipAccess.class.getName(), 7, Level.APPLICATIONERROR,
            "Can't activate the tenant", "The tenant activation failed", "Operation can't be done", "Check the error");

    private static BEvent eventCantDesactivateTenant = new BEvent(ContainerShipAccess.class.getName(), 8, Level.APPLICATIONERROR,
            "Can't desactivate the tenant", "The tenant desactivation failed", "Operation can't be done", "Check the error");

    private static BEvent eventTenantNotFound = new BEvent(ContainerShipAccess.class.getName(), 9, Level.APPLICATIONERROR,
            "Given tenant does not exist", "The tenant ID given does not exist", "Operation can't be done", "Give all informations");

    private static BEvent eventGeneralError = new BEvent(ContainerShipAccess.class.getName(), 10, Level.APPLICATIONERROR,
            "An error arrive during the execution", "Check Exception", "Operation can't be done", "Check the error");

    private static BEvent eventConnectionSuccess = new BEvent(ContainerShipAccess.class.getName(), 11, Level.SUCCESS,
            "Connection success", "Connection to the platform is correct", "Operation can't be done", "Check the password");

    private static BEvent eventTenantAlreadyExist = new BEvent(ContainerShipAccess.class.getName(), 12, Level.APPLICATIONERROR,
            "Tenant already exist", "A tenant with this name already exist", "Two tenants with the same name can't exist, tenant name must be unique", "Give a different name");

    private static BEvent eventOperationSuccess = new BEvent(ContainerShipAccess.class.getName(), 13, Level.SUCCESS,
            "Operation success", "The operation is a success");

    private static BEvent eventNoBonitaHome = new BEvent(ContainerShipAccess.class.getName(), 14, Level.ERROR,
            "No BonitaHome", "The variable BonitaHome can't be accessed",
            "The environmenent variable BONITA_HOME define the directory where the server install the ");

    private static BEvent eventTenantUpdate = new BEvent(ContainerShipAccess.class.getName(), 15, Level.ERROR,
            "No BonitaHome", "The variable BonitaHome can't be accessed",
            "The environmenent variable BONITA_HOME define the directory where the server install the ");

    private static BEvent eventTenantEditMissingParameter = new BEvent(ContainerShipAccess.class.getName(), 16, Level.APPLICATIONERROR,
            "Missing parameter to edit the tenant", "A set of minimal parameters has to be give to edit the tenant", "Operation can't be done", "Give all informations");

    private static final String cstParamServerUrl = "serverurl";
    private static final String cstParamApplicationName = "applicationname";
    private static final String cstParamPlatformUserName = "platformUsername";
    private static final String cstParamPlatformPassword = "platformPassword";

    private static final String cstParamTenantName = "name";
    private static final String cstParamTenantDescription = "description";
    private static final String cstParamTenantIconName = "conname";
    private static final String cstParamTenantIconPath = "iconpath";
    private static final String cstParamTenantUserName = "tenantUsername";
    private static final String cstParamTenantPassword = "tenantPassword";
    private static final String cstParamTenantActivate = "tenantActivate";
    private static final String cstParamTenantId = "id";

    public static class TenantParameters {

        String serverUrl;
        String applicationName;
        String platformUsername;
        String platformPassword;

        String tenantName;
        String tenantDescription;
        String tenantIconName;
        String tenantIconPath;
        String tenantUsername;
        String tenantPassword;
        boolean tenantActivate;

        String defaultBonitaHome;

        //public boolean includeTenantInfo = false;

        public boolean refreshListTenant = true;

        boolean isExternalCall = false;
        public APISession apiSession = null;

        Long tenantId;

        public static TenantParameters getInstance(final String jsonSt, final APISession apiSession)
        {

            logger.info("platform: JsonSt[" + jsonSt + "]");
            final TenantParameters tenantParameters = new TenantParameters();
            tenantParameters.apiSession = apiSession;
            if (jsonSt == null) {
                return tenantParameters;
            }
            @SuppressWarnings("unchecked")
            final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);
            if (jsonHash == null) {
                return tenantParameters;
            }
            tenantParameters.serverUrl = (String) jsonHash.get(cstParamServerUrl);
            tenantParameters.applicationName = (String) jsonHash.get(cstParamApplicationName);
            tenantParameters.platformUsername = (String) jsonHash.get(cstParamPlatformUserName);
            tenantParameters.platformPassword = (String) jsonHash.get(cstParamPlatformPassword);
            tenantParameters.tenantName = (String) jsonHash.get(cstParamTenantName);
            tenantParameters.tenantDescription = (String) jsonHash.get(cstParamTenantDescription);
            tenantParameters.tenantIconName = (String) jsonHash.get(cstParamTenantIconName);
            tenantParameters.tenantIconPath = (String) jsonHash.get(cstParamTenantIconPath);
            tenantParameters.tenantUsername = (String) jsonHash.get(cstParamTenantUserName);
            tenantParameters.tenantPassword = (String) jsonHash.get(cstParamTenantPassword);
            tenantParameters.tenantActivate = Toolbox.getBoolean(jsonHash.get(cstParamTenantActivate), Boolean.FALSE);
            tenantParameters.tenantId = Toolbox.getLong(jsonHash.get(cstParamTenantId), null);

            return tenantParameters;
        }

        @Override
        public String toString() {
            return "platformUsername[" + platformUsername + "]";
        }
    }

    /**
     *
     *
     */
    public static class TenantResult {

        // public Map<String, Object> result = new HashMap<String, Object>();
        public List<BEvent> listEvents = new ArrayList<BEvent>();
        public Long tenantId;
        public List<Tenant> listTenants = new ArrayList<Tenant>();
        Map<String, String> platformInfo = null;


        public Map<String, Object> getAnswer()
        {
            final Map<String, Object> answer = new HashMap<String, Object>();
            logger.info("TenantResult.getAnswer: tenantId=" + tenantId);
            final List<Map<String, Object>> listResultTenants = new ArrayList<Map<String, Object>>();
            if (listTenants != null)
             {
                for (final Tenant tenant : listTenants) {
                    final Map<String, Object> oneTenant = new HashMap<String, Object>();
                    listResultTenants.add(oneTenant);
                    oneTenant.put("id", tenant.getId());
                    oneTenant.put("name", tenant.getName());
                    oneTenant.put("description", tenant.getDescription());
                    oneTenant.put("state", tenant.getState().toString());
                    oneTenant.put("creationdate", tenant.getCreationDate().getTime());
                    if (tenantId != null && tenant.getId() == tenantId) {
                        oneTenant.put("isCurrent", Boolean.TRUE);
                    } else {
                        oneTenant.put("isCurrent", Boolean.FALSE);
                    }

                }
                // sort by name
            }

            Collections.sort(listResultTenants, new Comparator<Map<String, Object>>()
            {

                public int compare(final Map<String, Object> s1,
                        final Map<String, Object> s2)
                {
                    return ((String) s1.get("name")).compareToIgnoreCase((String) s2.get("name"));
                }
            });
            answer.put("listtenants", listResultTenants);
            answer.put("tenantid", tenantId);
            answer.put("platforminfo", platformInfo);

            answer.put("listeventshtml", BEventFactory.getHtml(listEvents));

            return answer;
        }

        public void addResult(final TenantResult tenantResultToAdd)
        {
            listEvents.addAll(tenantResultToAdd.listEvents);
            listTenants.addAll(tenantResultToAdd.listTenants);
            tenantId = tenantResultToAdd.tenantId;
        }
        @Override
        public String toString() {

            return "result=[" + getAnswer().toString() + "] events=" + listEvents.toString() + "]";
        }
    }

    /**
     * connect to the plaform
     *
     * @param tenantParameters
     * @return
     */
    public static TenantResult connection(final TenantParameters tenantParameters)
    {
        final TenantResult tenantResultPlatform = ContainerShipAccess.getPlatformInformation(tenantParameters);
        if (!BEventFactory.isError(tenantResultPlatform.listEvents))
        {
            final TenantResult tenantResultListTenants = ContainerShipAccess.getListTenants(tenantParameters);
            tenantResultPlatform.addResult(tenantResultListTenants);
            tenantResultPlatform.listEvents.add( eventConnectionSuccess);
        }
        return tenantResultPlatform;
    }

    /**
     * get the list of all tenants
     *
     * @param tenantParameters
     * @return
     */
    public static TenantResult getListTenants(final TenantParameters tenantParameters)
    {
        final TenantResult tenantResult = new TenantResult();
        PlatformSession session = null;
        PlatformAPI platformAPI = null;
        org.bonitasoft.engine.api.PlatformAPI plaformAPICommunity = null;

        try
        {
            session = connect(tenantParameters);
            plaformAPICommunity = com.bonitasoft.engine.api.PlatformAPIAccessor.getPlatformAPI(session);
            if (plaformAPICommunity instanceof com.bonitasoft.engine.api.PlatformAPI) {
                platformAPI = (PlatformAPI) plaformAPICommunity;
            } else {
                // content.append("Comunity server : the Tenant administration is not available<br>");
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }

            tenantResult.listTenants = platformAPI.getTenants(0, 10000, TenantCriterion.NAME_ASC);
            logger.info("ApiSession ?" + tenantParameters.apiSession + " TenantId="
                    + (tenantParameters.apiSession == null ? "<undefine" : tenantParameters.apiSession.getTenantId()));

            if (tenantParameters.apiSession != null) {
                tenantResult.tenantId = tenantParameters.apiSession.getTenantId();
            }

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        // } catch (BonitaHomeNotSetException | ServerAPIException | UnknownAPITypeException e)
        } catch (BonitaHomeNotSetException e)
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);
        } catch (ServerAPIException e)
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);
        } catch (UnknownAPITypeException e)
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);

        } catch (final Exception e)
        {
            logger.severe("Error during getlistTenants :" + e.toString());
        }
        logger.info("ContainerShipAccess.ListTenants:" + tenantResult.toString());

        return tenantResult;
    }

    /**
     *
     *
     */
    public enum TenantState {
        NOTEXIST, ACTIVATED, DEACTIVATED
    };

    public static class TenantInformation
    {


        public String name;
        public TenantState tenantState;
        public Tenant tenant;
    }

    public static TenantInformation getTenantByName(final TenantParameters tenantParameters, final String tenantName)
    {
        final TenantInformation tenantInformation = new TenantInformation();
        tenantInformation.name = tenantName;
        tenantInformation.tenantState = TenantState.NOTEXIST;
        final TenantResult tenantResult = ContainerShipAccess.getListTenants(tenantParameters);

        for (final Tenant tenant : tenantResult.listTenants)
        {
            if (tenantName.equals(tenant.getName()))
            {
                tenantInformation.tenant = tenant;

                final String state = tenant.getState();
                if ("ACTIVATED".equals(state)) {
                    tenantInformation.tenantState = TenantState.ACTIVATED;
                } else if ("DEACTIVATED".equals(state)) {
                    tenantInformation.tenantState = TenantState.DEACTIVATED;
                }
                return tenantInformation;
            }
        }
        logger.severe("No Tenant [" + tenantName + "] found");
        return tenantInformation;
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Operations on tenant */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public static TenantResult addTenant(final TenantParameters tenantParameters)
    {
        logBeginOperation("addTenant", "", tenantParameters);
        String logDetails = "";
        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            long beginTime = System.currentTimeMillis();
            final PlatformAPI platformAPI = getPlatformAPI(tenantParameters);

            if (platformAPI == null) {
                 eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }


            logDetails += "tenantName[" + tenantParameters.tenantName + "] tenantDescription[" + tenantParameters.tenantDescription
                    + "] tenantIconName[" + tenantParameters.tenantIconName
                    + "] tenantIconPath[" + tenantParameters.tenantIconPath
                    + "] tenantUsername[" + tenantParameters.tenantUsername
                    + "] tenantPassword[*******" 
                    + "] Activate ? [" + tenantParameters.tenantActivate + "]";
            logger.info("ADD Tenant " + logDetails);

            // because we can't trust the engine (which return a CreateException and in fact create the tenant), we check before the parameters
            String errorParameters = "";
            if (tenantParameters.tenantName == null || tenantParameters.tenantName.trim().length() == 0) {
                errorParameters += "No Tenant Name;";
            }
            if (tenantParameters.tenantDescription == null || tenantParameters.tenantDescription.trim().length() == 0) {
                errorParameters += "No Tenant Description;";
            }
            if (tenantParameters.tenantUsername == null || tenantParameters.tenantUsername.trim().length() == 0) {
                errorParameters += "No Tenant Username;";
            }
            if (tenantParameters.tenantPassword == null || tenantParameters.tenantPassword.trim().length() == 0) {
                errorParameters += "No Tenant Password;";
            }

            // BonitaHome : only before 7.3
            String bonitaHome = null;
            if (isBonitaHome(platformAPI))
            {
                bonitaHome = System.getProperty(BonitaHome.BONITA_HOME);
                if (bonitaHome == null) {
                    bonitaHome = tenantParameters.defaultBonitaHome;
                }
                if (bonitaHome == null) {
                    errorParameters += "Can't access BONITA_HOME;";
                    tenantResult.listEvents.add( eventNoBonitaHome);
                }
            }

            if (errorParameters.length() > 0)
            {
                final BEvent eventMissingParameters = new BEvent( eventTenantCreationMissingParameter, errorParameters);
                eventMissingParameters.log();
                tenantResult.listEvents.add( eventMissingParameters);
                return tenantResult;
            }

            tenantResult.tenantId = platformAPI.createTenant(new TenantCreator(tenantParameters.tenantName, tenantParameters.tenantDescription,
                    tenantParameters.tenantIconName, tenantParameters.tenantIconPath, tenantParameters.tenantUsername,
                    tenantParameters.tenantPassword));

            // ATTENTION : at this point, the JAVA call does not completely duplicate all file from the Tenant-Template... RESTAPI doe, not the Java
            // in order to complete this bu... sorry, this "not implemented feature", let's do now the copy


            final ToolboxFile.CopyFolderParameter copyFolderParameters = new ToolboxFile.CopyFolderParameter();
            copyFolderParameters.overwrite = false;
            copyFolderParameters.reportOnlyError = true;
            copyFolderParameters.destinationFolderMustExist = false;// client does not exist

            if (bonitaHome != null)
            {
                tenantResult.listEvents.addAll(ToolboxFile.copyFolder(bonitaHome + "/client/platform/tenant-template",
                        bonitaHome + "/client/tenants/" + tenantResult.tenantId,
                        copyFolderParameters));

                copyFolderParameters.destinationFolderMustExist = true;// server exist
                tenantResult.listEvents.addAll(ToolboxFile.copyFolder(bonitaHome + "/engine-server/conf/tenants/template",
                        bonitaHome + "/engine-server/conf/tenants/" + tenantResult.tenantId,
                        copyFolderParameters));
            }

            // now activate the tenant
            if (tenantParameters.tenantActivate) {
                platformAPI.activateTenant(tenantResult.tenantId);
            }
            long endTime = System.currentTimeMillis();

            if (!BEventFactory.isError(tenantResult.listEvents)) {
                tenantResult.listEvents.add(new BEvent( eventOperationSuccess, "New tenant id[" + tenantResult.tenantId + "] created [" + logDetails + "] in "+(endTime - beginTime )+" ms"));
            }
            // get the list of tenant ?
            logger.info("refreshListTenant list[" + tenantParameters.refreshListTenant + "]");
            if (tenantParameters.refreshListTenant)
            {
                tenantResult.addResult(getListTenants(tenantParameters));
            }
        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (final AlreadyExistsException ae)
        {
            logger.severe("Already exist tenant [" + logDetails + "] :" + ae.toString());
            tenantResult.listEvents.add(new BEvent( eventTenantAlreadyExist, ae, logDetails));

        } catch (final CreationException ce)
        {
            logger.severe("Can't create the tenant [" + logDetails + "] :" + ce.toString());
            tenantResult.listEvents.add(new BEvent( eventCantCreationTenant, ce, logDetails));
        } catch (final Exception e)
        {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();

            logger.severe("Error during addTenant :" + exceptionDetails);
            tenantResult.listEvents.add(new BEvent( eventGeneralError, e, ""));

        }
        logEndOperation("addTenant", " newTenantId[" + tenantResult.tenantId + "]", tenantResult);

        return tenantResult;

    }

    public static TenantResult editTenant(final TenantParameters tenantParameters)
    {
        logBeginOperation("editTenant", "", tenantParameters);
        String logDetails = "";
        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            final PlatformAPI platformAPI = getPlatformAPI(tenantParameters);

            if (platformAPI == null) {
                 eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }

            logDetails += "tenantName[" + tenantParameters.tenantName + "] tenantDescription[" + tenantParameters.tenantDescription
                    + "] tenantIconName[" + tenantParameters.tenantIconName
                    + "] tenantIconPath[" + tenantParameters.tenantIconPath
                    + "] tenantUsername[" + tenantParameters.tenantUsername
                    + "] tenantPassword[" + tenantParameters.tenantPassword
                    + "] Activate ? [" + tenantParameters.tenantActivate + "]";
            logger.info("EDIT Tenant " + logDetails);

            // because we can't trust the engine (which return a CreateException and in fact create the tenant), we check before the parameters
            String errorParameters = "";
            if (tenantParameters.tenantName == null || tenantParameters.tenantName.trim().length() == 0) {
                errorParameters += "No Tenant Name;";
            }
            if (tenantParameters.tenantDescription == null || tenantParameters.tenantDescription.trim().length() == 0) {
                errorParameters += "No Tenant Description;";
            }
          // acceptable to  not have the tenantUserName and the tenantpassword

            // BonitaHome : only before 7.3
            String bonitaHome = null;
            final String bonitaVersion = platformAPI.getPlatform().getVersion();
            logger.info("BonitaVersion[" + bonitaVersion + "]");
            if (bonitaVersion.startsWith("6.") || bonitaVersion.startsWith("7.0")
                    || bonitaVersion.startsWith("7.1")
                    || bonitaVersion.startsWith("7.2"))
            {

                bonitaHome = System.getProperty(BonitaHome.BONITA_HOME);
                if (bonitaHome == null) {
                    bonitaHome = tenantParameters.defaultBonitaHome;
                }
                if (bonitaHome == null) {
                    errorParameters += "Can't access BONITA_HOME;";
                    tenantResult.listEvents.add( eventNoBonitaHome);
                }
            }

            if (errorParameters.length() > 0)
            {
                final BEvent eventMissingParameters = new BEvent( eventTenantEditMissingParameter, errorParameters);
                eventMissingParameters.log();
                tenantResult.listEvents.add( eventMissingParameters);
                return tenantResult;
            }

            final TenantUpdater tenantUpdater = new TenantUpdater();
            tenantUpdater.setName(tenantParameters.tenantName);
            tenantUpdater.setDescription(tenantParameters.tenantDescription);

            if (tenantParameters.tenantUsername != null && tenantParameters.tenantUsername.length() > 0) {
                tenantUpdater.setUsername(tenantParameters.tenantUsername);
            }

            if (tenantParameters.tenantPassword != null && tenantParameters.tenantPassword.length() > 0) {
                tenantUpdater.setPassword(tenantParameters.tenantPassword);
            }

            platformAPI.updateTenant(tenantParameters.tenantId, tenantUpdater);
            // get the list of tenant ?
            logger.info("refreshListTenant list[" + tenantParameters.refreshListTenant + "]");
            tenantResult.listEvents.add(new BEvent( eventOperationSuccess, "Tenant [" + tenantParameters.tenantId + "] Updated"));

            if (tenantParameters.refreshListTenant)
            {
                tenantResult.addResult(getListTenants(tenantParameters));
            }

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (final UpdateException ae)
        {
            logger.severe("Update exception " + logDetails + "] :" + ae.toString());
            tenantResult.listEvents.add(new BEvent( eventTenantUpdate, ae, logDetails));

        } catch (BonitaHomeNotSetException e )
            {
                logger.severe("Error during getlistTenants " + e.toString());
                tenantResult.listEvents.add( eventBadPlatform);
            } catch (ServerAPIException e )
            {
                logger.severe("Error during getlistTenants " + e.toString());
                tenantResult.listEvents.add( eventBadPlatform);
            } catch (UnknownAPITypeException e )
            {
                logger.severe("Error during getlistTenants " + e.toString());
                tenantResult.listEvents.add( eventBadPlatform);
            
        } catch (final Exception e)
        {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();

            logger.severe("Error during addTenant :" + exceptionDetails);
            tenantResult.listEvents.add(new BEvent( eventGeneralError, e, ""));

        }
        logEndOperation("addTenant", " newTenantId[" + tenantResult.tenantId + "]", tenantResult);

        return tenantResult;

    }
    /**
     * @param tenantParameters
     * @return
     */
    public static TenantResult activateTenant(final TenantParameters tenantParameters)
    {
        logBeginOperation("activateTenant", "", tenantParameters);
        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            final PlatformAPI platformAPI = getPlatformAPI(tenantParameters);

            if (platformAPI == null) {
                 eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }

            platformAPI.activateTenant(tenantParameters.tenantId);
            tenantResult.listEvents.add(new BEvent( eventOperationSuccess, "Tenant [" + tenantParameters.tenantId + "] Activate"));

            // get the list of tenant ?
            logger.info("refreshListTenant list[" + tenantParameters.refreshListTenant + "]");
            if (tenantParameters.refreshListTenant)
            {
                tenantResult.addResult(getListTenants(tenantParameters));
            }

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (final TenantActivationException te)
        {
            logger.severe("Can't activate the tenant [" + tenantResult.tenantId + "] : " + te.toString());
            tenantResult.listEvents.add(new BEvent( eventCantActivateTenant, te, "TenantId[" + tenantResult.tenantId + "]"));
        } catch (final TenantNotFoundException tnotfounde)
        {
            logger.severe("Can't activate the tenant : not found [" + tenantResult.tenantId + "] : " + tnotfounde.toString());
            tenantResult.listEvents.add(new BEvent( eventTenantNotFound, tnotfounde, "TenantId[" + tenantResult.tenantId + "]"));
        } catch (BonitaHomeNotSetException e )
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);
        } catch (ServerAPIException e )
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);
        } catch (UnknownAPITypeException e )
        {
            logger.severe("Error during getlistTenants " + e.toString());
            tenantResult.listEvents.add( eventBadPlatform);
        
        } catch (final Exception e)
        {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();

            logger.severe("Error during activateTenant :" + exceptionDetails);
            tenantResult.listEvents.add(new BEvent( eventGeneralError, e, ""));
        }
        logEndOperation("activateTenant", "", tenantResult);

        return tenantResult;
    }

    /**
     * @param tenantParameters
     * @return
     */
    public static TenantResult desactivateTenant(final TenantParameters tenantParameters)
    {
        logBeginOperation("desactivateTenant", "", tenantParameters);

        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            final PlatformAPI platformAPI = getPlatformAPI(tenantParameters);

            if (platformAPI == null) {
                 eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }
            if (tenantParameters.tenantId == null)
            {
                tenantResult.listEvents.add(new BEvent( eventTenantNotFound, "tenantId is not give as parameters"));
                return tenantResult;

            }
            platformAPI.deactiveTenant(tenantParameters.tenantId);
            tenantResult.listEvents.add(new BEvent( eventOperationSuccess, "Tenant [" + tenantParameters.tenantId + "] Deactivate"));
            // get the list of tenant ?
            logger.info("refreshListTenant list[" + tenantParameters.refreshListTenant + "]");
            if (tenantParameters.refreshListTenant)
            {
                tenantResult.addResult(getListTenants(tenantParameters));
            }

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (final TenantDeactivationException tde)
        {
            logger.severe("Can't desactivate the tenant [" + tenantResult.tenantId + "] : " + tde.toString());
            tenantResult.listEvents.add(new BEvent( eventCantDesactivateTenant, tde, "TenantId[" + tenantResult.tenantId + "]"));
        } catch (final TenantNotFoundException tnotfounde)
        {
            logger.severe("Can't activate the tenant : not found [" + tenantResult.tenantId + "] : " + tnotfounde.toString());
            tenantResult.listEvents.add(new BEvent( eventTenantNotFound, tnotfounde, "TenantId[" + tenantResult.tenantId + "]"));
        } catch (BonitaHomeNotSetException e)
        {
            logger.severe("Error during desactivateTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch (ServerAPIException e)
        {
            logger.severe("Error during desactivateTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch (UnknownAPITypeException e)
        {
            logger.severe("Error during desactivateTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch (final Exception e)
        {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();

            logger.severe("Error during desactivateTenant :" + exceptionDetails);
            tenantResult.listEvents.add(new BEvent( eventGeneralError, e, ""));

        }
        logEndOperation("desactivateTenant", "", tenantResult);

        return tenantResult;
    }

    /**
     * @param tenantParameters. We use only tenantId in the parameters
     * @return
     */
    public static TenantResult removeTenant(final TenantParameters tenantParameters)
    {
        logBeginOperation("removeTenant", "", tenantParameters);
        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            String bonitaHome = null;

            final PlatformAPI platformAPI = getPlatformAPI(tenantParameters);
            if (isBonitaHome(platformAPI))
            {
                bonitaHome = System.getProperty(BonitaHome.BONITA_HOME);
            if (bonitaHome == null) {
                bonitaHome = tenantParameters.defaultBonitaHome;
            }
            if (bonitaHome == null) {
                tenantResult.listEvents.add( eventNoBonitaHome);
                return tenantResult;
            }
            }

            if (platformAPI == null) {
                 eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }

            try
            {
                platformAPI.deactiveTenant(tenantParameters.tenantId);
            } catch (final Exception e)
            {
                // maybe already deactivate
            }
            platformAPI.deleteTenant(tenantParameters.tenantId);
            tenantResult.listEvents.add(new BEvent( eventOperationSuccess, "Tenant [" + tenantParameters.tenantId + "] removed"));

            // purge directory : the delete tenant does not do the job on client side
            if (bonitaHome != null)
            {
                tenantResult.listEvents.addAll(ToolboxFile.deleteFolder(bonitaHome + "/client/tenants/" + tenantParameters.tenantId));

                tenantResult.listEvents.addAll(ToolboxFile.deleteFolder(bonitaHome + "/engine-server/conf/tenants/" + tenantParameters.tenantId));
            }
            // get the list of tenant ?
            logger.info("refreshListTenant list[" + tenantParameters.refreshListTenant + "]");
            if (tenantParameters.refreshListTenant)
            {
                tenantResult.addResult(getListTenants(tenantParameters));
            }

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (final DeletionException de)
        {
            logger.severe("Can't delete the tenant [" + tenantResult.tenantId + "] : " + de.toString());
            tenantResult.listEvents.add(new BEvent( eventCantDeleteTenant, de, "TenantId[" + tenantResult.tenantId + "]"));
        } catch (BonitaHomeNotSetException e)
        {
            logger.severe("Error during removeTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch ( ServerAPIException  e)
        {
            logger.severe("Error during removeTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch (UnknownAPITypeException e)
        {
            logger.severe("Error during removeTenant " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));

        } catch (final Exception e)
        {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();

            logger.severe("Error during removeTenant :" + exceptionDetails);
            tenantResult.listEvents.add(new BEvent( eventGeneralError, e, ""));

        }
        logEndOperation("removeTenant", "", tenantResult);
        return tenantResult;
    }

    /**
     * @param tenantParameters
     * @return
     */
    public static TenantResult getPlatformInformation(final TenantParameters tenantParameters)
    {
        logBeginOperation("getPlatformInformation", "", tenantParameters);

        // connection
        final TenantResult tenantResult = new TenantResult();
        try
        {
            final PlatformAPI platformAPI = getPlatformAPI( tenantParameters );

           if (platformAPI == null) {
                eventTenantAdminNotAvailable.log();
                tenantResult.listEvents.add( eventTenantAdminNotAvailable);
                return tenantResult;
            }
            tenantResult.platformInfo = platformAPI.getInformation();

            tenantResult.platformInfo.put("platformstate", platformAPI.getPlatformState().toString());
            tenantResult.platformInfo.put("platformversion", platformAPI.getPlatform().getVersion());

            logger.info("Platform Insformation [" + tenantResult.platformInfo + "]");

        } catch (final PlatformLoginException le)
        {
            logger.severe("Can't connect with user [" + tenantParameters.platformUsername + "]");
            tenantResult.listEvents.add( eventPlatformLogin);
        } catch (BonitaHomeNotSetException e)
        {
            logger.severe("Error during getPlatformInformation " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch ( ServerAPIException e)
        {
            logger.severe("Error during getPlatformInformation " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));
        } catch (UnknownAPITypeException e)
        {
            logger.severe("Error during getPlatformInformation " + e.toString());
            tenantResult.listEvents.add(new BEvent( eventBadPlatform, e, ""));

        } catch (final Exception e)
        {
            logger.severe("Error during getPlatformInformation :" + e.toString());
        }
        logEndOperation("getPlatformInformation", "", tenantResult);

        return tenantResult;
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Connect */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * connect, with the user platform, saved in <BONITA>/engine-server/work/platform/bonita-platform-community.properties
     *
     * @param tenantParameters
     * @return
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     * @throws InvalidPlatformCredentialsException
     * @throws PlatformLoginException
     */
    private static PlatformAPI getPlatformAPI(final TenantParameters tenantParameters) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, PlatformLoginException
    {
        // connect !
        final PlatformSession session = connect(tenantParameters);

        // get the PlatformAPI bound to the session created previously.
        final org.bonitasoft.engine.api.PlatformAPI plaformAPICommunity = com.bonitasoft.engine.api.PlatformAPIAccessor.getPlatformAPI(session);
        if (plaformAPICommunity instanceof com.bonitasoft.engine.api.PlatformAPI) {
            return (PlatformAPI) plaformAPICommunity;
        } else {
            return null;
        }
    }

    private static PlatformSession connect(final TenantParameters tenantParameters) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, InvalidPlatformCredentialsException, PlatformLoginException
    {
        if (tenantParameters.isExternalCall)
        {
            final Map<String, String> map = new HashMap<String, String>();
            map.put("server.url", tenantParameters.serverUrl);
            map.put("application.name", tenantParameters.applicationName);
            APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, map);
        }
        // get the PlatformLoginAPI using the PlatformAPIAccessor
        Object platformLoginAPIObj = PlatformAPIAccessor.getPlatformLoginAPI();
        if (platformLoginAPIObj instanceof  org.bonitasoft.engine.api.PlatformLoginAPI )
        {
        	PlatformSession session= ((org.bonitasoft.engine.api.PlatformLoginAPI) platformLoginAPIObj).login(tenantParameters.platformUsername, tenantParameters.platformPassword);
        	return session;
        }
        else 
        {
        	return null;
        }
        // log in to the platform to create a section
        // final PlatformSession session = platformLoginAPI.login(tenantParameters.platformUsername, tenantParameters.platformPassword);
        // return session;
    }

    /**
     * normalize beginLog
     *
     * @param method
     * @param message
     * @param foodTruckParam
     */
    private static void logBeginOperation(final String method, final String message, final TenantParameters tenantParameters)
    {
        logger.info("~~~~~~~~ START 20160815 V1.2  ContainerShipAccess." + method + ": " + message
                + (tenantParameters != null ? " param[" + tenantParameters.toString() + "]" : ""));
    }

    /**
     * logEndOperation
     *
     * @param method
     * @param message
     * @param foodTruckResult
     */
    private static void logEndOperation(final String method, final String message, final TenantResult tenantResult)
    {
        logger.info("~~~~~~~~ END V1.2 ContainerShipAccess." + method + ":  status [" + tenantResult.toString() + "] " + message + " events["
                + tenantResult.listEvents + "]");

    }

    /**
     * This is a Bonita Home when this is under 7.3
     * @param platformAPI
     * @return
     * @throws PlatformNotFoundException
     */
    private static boolean isBonitaHome(final PlatformAPI platformAPI) throws PlatformNotFoundException
    {
        final String bonitaVersion = platformAPI.getPlatform().getVersion();
        logger.info("BonitaVersion[" + bonitaVersion + "]");
        // get the first number
        StringTokenizer st = new StringTokenizer(bonitaVersion,".");
        try
        {
            int version = st.hasMoreTokens()? Integer.valueOf( st.nextToken()):0;
            int release = st.hasMoreTokens()? Integer.valueOf( st.nextToken()):0;
            if (version==0)
                return false;
            if (version < 7 || (version==7 && release < 3))
                return true;
            return false;
        }
        catch(Exception e) {
            return false;
        }
    }
}
