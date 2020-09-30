import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Runtime;

import org.json.simple.JSONObject;
import org.codehaus.groovy.tools.shell.CommandAlias;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;


import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

import org.apache.commons.lang3.StringEscapeUtils
 
import org.bonitasoft.engine.identity.User;

import org.bonitasoft.web.extension.page.PageContext;
import org.bonitasoft.web.extension.page.PageController;
import org.bonitasoft.web.extension.page.PageResourceProvider;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;


import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
	
import com.bonitasoft.custompage.containership.ContainerShipAccess;

import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantParameters;
import com.bonitasoft.custompage.containership.ContainerShipAccess.TenantResult;

public class Actions {

	private static Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
	
	
	public static Index.ActionAnswer doAction(HttpServletRequest request, String paramJsonSt, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
				
		logger.info("#### ContainerShip:Actions start");
		Index.ActionAnswer actionAnswer = new Index.ActionAnswer();	
		try {
			String action=request.getParameter("action");
			logger.info("#### ContainerShip:Actions  action is["+action+"] !");
			if (action==null || action.length()==0 )
			{
				actionAnswer.isManaged=false;
				logger.info("#### ContainerShip:Actions END No Actions");
				return actionAnswer;
			}
			actionAnswer.isManaged=true;
			
			APISession session = pageContext.getApiSession()
			ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
			CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
			BusinessDataAPI businessDataAPI = TenantAPIAccessor.getBusinessDataAPI(session);
			PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI(session);

			if ("getplatforminformation".equals(action))
			{
				TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
				TenantResult tenantResult = ContainerShipAccess.getPlatformInformation( tenantParameters);
				actionAnswer.setResponse( tenantResult.getAnswer() );
			}
            else if ("getlisttenants".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
                TenantResult tenantResult = ContainerShipAccess.getListTenants( tenantParameters);
                actionAnswer.setResponse( tenantResult.getAnswer());
            }
			else if ("connection".equals(action))
			{
				TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
				TenantResult tenantResultPlatform = ContainerShipAccess.connection( tenantParameters);
				actionAnswer.setResponse( tenantResultPlatform.getAnswer() );
				
			} else if ("addtenant".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
                TenantResult tenantResultPlatform = ContainerShipAccess.addTenant( tenantParameters);
                actionAnswer.setResponse( tenantResultPlatform.getAnswer() );
                
            } else if ("edittenant".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
                TenantResult tenantResultPlatform = ContainerShipAccess.editTenant( tenantParameters);
                actionAnswer.setResponse( tenantResultPlatform.getAnswer());
                
            } else if ("activatetenant".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
                TenantResult tenantResultPlatform = ContainerShipAccess.activateTenant( tenantParameters);
                actionAnswer.setResponse( tenantResultPlatform.getAnswer() );
                
            } else if ("desactivateTenant".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt, session);
                TenantResult tenantResultPlatform = ContainerShipAccess.desactivateTenant( tenantParameters);
                actionAnswer.setResponse( tenantResultPlatform.getAnswer());
                
            } else if ("removetenant".equals(action))
            {
                TenantParameters tenantParameters = TenantParameters.getInstance(paramJsonSt,session);
                TenantResult tenantResultPlatform = ContainerShipAccess.removeTenant( tenantParameters);
                actionAnswer.setResponse( tenantResultPlatform.getAnswer() );
            } 
            
			logger.info("#### ContainerShip:Actions END responseMap ="+actionAnswer.responseMap.size());
			return actionAnswer;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.severe("#### LongBoardCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
			actionAnswer.isResponseMap=true;
			actionAnswer.responseMap.put("Error", "LongBoardCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
			return actionAnswer;
		}
	}

	
	
	
	
}
