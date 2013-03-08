/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api.resource.helper;

import be.libis.primo.api.resource.SessionResource;
import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.delivery.Institution;
import com.exlibris.primo.domain.delivery.InstitutionIP;
import com.exlibris.primo.domain.reference.HMappingTables;
import com.exlibris.primo.domain.views.Views;
import com.exlibris.primo.facade.CodeTablesManagementFacade;
import com.exlibris.primo.facade.InstitutionsManagementFacade;
import com.exlibris.primo.pds.PdsUserInfo;
import com.exlibris.primo.server.facade.ViewsManagementFacade;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.xsd.primoview.config.ViewDocument.View;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mehmetc
 */
public class SessionParameters {

    private String id;
    private boolean loggedOn;
    private boolean onCampus;
    private String view;
    private String institutionName;
    private String institutionCode;
    private String institutionNameByIP;
    private String institutionCodeByIP;
    private String institutionNameByView;
    private String institutionCodeByView;    
    private PdsUserInfo userInfo;
    private String pdsUrl;
    private String interfaceLanguage;
    private String sfxInstitutionCode;
    private String metalibInstitutionCode;
    private Institution institution;

    public SessionParameters(HttpServletRequest request) {
        this.id = SessionUtils.getSessionId(request);
        this.onCampus = Boolean.valueOf(SessionUtils.getOnCampus(request)).booleanValue();
        this.loggedOn = SessionUtils.getIsLoggedIn(request);
        this.view = SessionUtils.getSessionViewId(request);
        this.institutionName = SessionUtils.getInstitutionName(request);
        this.institutionCode = SessionUtils.getInstitutionCode(request);
        this.userInfo = SessionUtils.getUserInfo(request);
        this.pdsUrl = SessionUtils.getPDSUrl(request);
        this.interfaceLanguage = SessionUtils.getChosenInterfaceLanguage(request);
        this.institution = SessionUtils.getInstitution(request);

        CodeTablesManagementFacade ctmf = (CodeTablesManagementFacade) ContextAccess.getInstance().getCodeTablesManagementFacade();
        List<HMappingTables> sfxInstitutes = ctmf.findMappingsByTableNameAndTargetEnabled("SFX Institutes", this.institutionCode);
        if (sfxInstitutes.size() > 0) {
            this.sfxInstitutionCode = sfxInstitutes.get(0).getSourceCode1();
        }

        List<HMappingTables> metalibInstitutes = ctmf.findMappingsByTableNameAndTargetEnabled("MetaLib Institution Codes", this.institutionCode);
        if (metalibInstitutes.size() > 0) {
            this.metalibInstitutionCode = metalibInstitutes.get(0).getSourceCode1();
        }

        ViewsManagementFacade views = (ViewsManagementFacade) ContextAccess.getInstance().getBean("ViewsManagementFacade");
        try {
            List<Views> viewsList = views.findViewByViewCode(this.view);
            Views vv =viewsList.get(0);
        
            Institution i  = vv.getInstitutions();
            this.institutionCodeByView = i.getInstitutionCode();
            this.institutionNameByView = i.getInstitutionName();                        
        } catch (Exception ex) {
            Logger.getLogger(SessionResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        InstitutionsManagementFacade institutions = (InstitutionsManagementFacade) ContextAccess.getInstance().getBean("institutionsManagementFacade");
        try {
            InetAddress ipAddress = InetAddress.getByName(request.getRemoteAddr());
            InstitutionIP institutionByIP = institutions.findInstitution(ipAddress);            

            Institution i = institutionByIP.getInstitution();
            
            if (institution != null){
                this.institutionCodeByIP = i.getInstitutionCode();
                this.institutionNameByIP = i.getInstitutionName();
            }

        } catch (Exception ex) {
            Logger.getLogger(SessionResource.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @return the loggedOn
     */
    public boolean isLoggedOn() {
        return loggedOn;
    }

    /**
     * @return the onCampus
     */
    public boolean isOnCampus() {
        return onCampus;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * @return the institutionName
     */
    public String getInstitutionName() {
        return institutionName;
    }

    /**
     * @return the institutionCode
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * @return the institutionNameByIP
     */
    public String getInstitutionNameByIP() {
        return institutionNameByIP;
    }

    /**
     * @return the institutionCodeByIP
     */
    public String getInstitutionCodeByIP() {
        return institutionCodeByIP;
    }
}
