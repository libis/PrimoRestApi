/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api.resource.helper;

import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.entities.HRemoteSourceRecord;
import com.exlibris.primo.domain.entities.HSourceRecord;
import com.exlibris.primo.exceptions.PersistenceException;
import com.exlibris.primo.facade.PnxManagementFacade;
import com.exlibris.primo.srvinterface.PrimoPnxHelper;
import com.exlibris.primo.srvinterface.RecordDocDTO;
import com.exlibris.primo.utils.CommonUtil;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.xsd.commonData.PrimoResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author mehmetc
 */
public class RecordResourceHelper {

    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    public List<RecordDocDTO> all(HttpServletRequest request) {
        return SessionUtils.getSearchFrom(request).getSearchResult().getResults();
    }

    public RecordDocDTO getRecordDocDTO(String recordID, HttpServletRequest request) {
        List<? extends RecordDocDTO> allRecords = this.all(request);

        for (RecordDocDTO record : allRecords) {
            if (record.getId().equals(recordID)) {
                return record;
            }
        }
        return null;
    }

    public String getOriginalRecord(String recordID) {
        String record = "<error>no record</error>";
        List resultList = new ArrayList();

        if (!CommonUtil.isNotLocalRecord(recordID)) {
            resultList = ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.recordId = ?", new Object[]{recordID});
            if (resultList.size() > 0) {
                record = ((HSourceRecord) resultList.get(0)).getXmlContent();
            }
        } else {
            resultList = ContextAccess.getInstance().getPersistenceManager().find("from HRemoteSourceRecord record where record.recordId = ?", new Object[]{recordID});

            if (resultList.size() > 0) {
                //record = ((HRemoteSourceRecord) resultList.get(0)).getStringClobField();
                record = ((HRemoteSourceRecord) resultList.get(0)).getXmlContent();
            }
        }
        return record;
    }

    public String getPNXRecordByID(String recordID, HttpServletRequest request) {
        String record = "";
        if (CommonUtil.isNotLocalRecord(recordID)) {
            PrimoResult tmpResult = PrimoPnxHelper.getRecordDoc(recordID);
            if (tmpResult == null) {
                PrimoResult primoResult = SessionUtils.getSearchResult(request);
                if (primoResult != null) {
                    record = primoResult.toString();

                    //Remove every namespace and namespace prefix because the namespacing in returned documents are strange.
                    String patch = "xmlns:prim=\"http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib\" xmlns:sear=\"http://www.exlibrisgroup.com/xsd/jaguar/search\"";
                    String badNamespace = "xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\"";
                    String badNamespace2 = "xmlns=\"http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib\"";
                    if (record.contains(badNamespace)) {
                        record = record.replaceFirst(badNamespace, patch);
                        record = record.replaceAll(patch, "");
                        record = record.replaceAll(badNamespace, "");
                        record = record.replaceAll(badNamespace2, "");
                        record = record.replaceAll("sear:", "");
                        record = record.replaceAll("prim:", "");
                    }


                    try {
                        File testFile = new File("/tmp/pnx_" + recordID + ".xml");
                        FileWriter fw = new FileWriter(testFile);

                        fw.write(record);

                        fw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(RecordResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    Document xmlDoc = XMLHelper.toXml(record);
                    if (xmlDoc != null) {
                        String xml = XMLHelper.xpath(xmlDoc, "//record[control/recordid='" + recordID + "']");

                        if (xml != null && xml.length() > 0) {
                            record = xml;
                        } else {
                            record = getOriginalRecord(recordID);
                        }
                    }
                }
            }
        } else {
            PnxManagementFacade pnxManagementFacade = (PnxManagementFacade) ContextAccess.getInstance().getPnxManagementFacade();
            List pnxRecords = null;
            try {
                pnxRecords = pnxManagementFacade.getPnxRecords(recordID, null, null, 1);
            } catch (PersistenceException ex) {
                Logger.getLogger(RecordResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!(pnxRecords.isEmpty())) {
                HSourceRecord sourceRecord = (HSourceRecord) pnxRecords.iterator().next();
                record = sourceRecord.getXmlContent();
            }
        }
        return record;
    }
}
