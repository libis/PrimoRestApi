/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api.resource.helper;

import com.exlibris.jaguar.xsd.search.DOCDocument.DOC;
import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.entities.HRemoteSourceRecord;
import com.exlibris.primo.domain.entities.HSourceRecord;
import com.exlibris.primo.domain.entities.OriginalSourceRecord;
import com.exlibris.primo.facade.PnxManagementFacade;
import com.exlibris.primo.srvinterface.RecordDocDTO;
import com.exlibris.primo.utils.CommonUtil;
import com.exlibris.primo.utils.SessionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;


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

    public List getRecordIDSFromDedupID(String dedupID){
        List<String> response = new ArrayList();
                
        List resultList = new ArrayList();
        long recordID = Long.parseLong(dedupID.replaceAll("dedupmrg", ""));
        resultList = ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.matchId = ?", new Object[]{recordID});        
        
        if (resultList.size() > 0) {
            for (Object record: resultList){
                if (!response.contains(((HSourceRecord)record).getSourceId())) {
                    response.add(((HSourceRecord)record).getSourceId());
                }
            }
        }
        
        return response;
    }
    
    public String getOriginalRecord(String recordID) {
        String record = "<error>no record</error>";
        List resultList = new ArrayList();

        if (!CommonUtil.isNotLocalRecord(recordID)) {
            resultList = ContextAccess.getInstance().getPersistenceManager().find("from OriginalSourceRecord record where record.recordID = ?", new Object[]{recordID});
            //resultList = ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.recordId = ?", new Object[]{recordID});
            record = ((HSourceRecord)ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.recordId = ?", new Object[]{recordID}).get(0)).getXmlContent();
            if (resultList.size() > 0) {
                record = ((OriginalSourceRecord) resultList.get(0)).getSourceRecord();
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

    public HashMap<String,String> getPNXRecordList(HttpServletRequest request) {
        HashMap<String,String> records = new HashMap<String,String>();
        try {
            DOC[] docArray = SessionUtils.getSearchResult(request).getSEGMENTS().getJAGROOTArray(0).getRESULT().getDOCSET().getDOCArray();
            for (DOC doc : docArray) {
                String id = doc.getPrimoNMBib().getRecordArray(0).getControl().getRecordidArray(0);
                records.put(id, doc.getPrimoNMBib().xmlText());            
            }
        } catch (Exception ex) {
                Logger.getLogger(RecordResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return records;
    }
    
    public String getPNXRecordByID(String recordID, HttpServletRequest request) {
        String record = "";
        if (CommonUtil.isNotLocalRecord(recordID)) { //do this if it is a remote record
            record = getPNXRecordList(request).get(recordID);
            record = record.replaceAll("prim:", ""); //remove annoying useless prefix
        } else { // if it is local get it from fresh from the database works also without a resultset.
            PnxManagementFacade pnxManagementFacade = (PnxManagementFacade) ContextAccess.getInstance().getPnxManagementFacade();
            List pnxRecords = null;
            try {
                pnxRecords = pnxManagementFacade.getPnxRecords(recordID, null, null, 1);                
            } catch (Exception ex) {
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
