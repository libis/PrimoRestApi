/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api.resource;

import be.libis.primo.api.resource.helper.RecordResourceHelper;
import com.exlibris.primo.srvinterface.PrimoPnxHelper;
import com.exlibris.primo.srvinterface.RecordDocDTO;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.utils.formats.Ris2;
import com.exlibris.primo.xsd.commonData.PrimoResult;
import com.google.gson.Gson;
import com.sun.jersey.core.util.Base64;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author mehmetc
 * 
 * 
 * add
 * <mapping resource="com/exlibris/primo/domain/entities/OriginalSourceRecord.hbm.xml"/>
 * to /exlibris/primo/p3_1/ng/primo/home/system/search/conf/hibernate.cfg.xml
 * 
 */
//jQuery.getJSON('/primo_library/libweb/rest/records', function(data){jQuery.each(data, function(){console.log(this.id);})});
@Path("/records")
public class RecordResource {

    @Context UriInfo uriInfo;
    @Context HttpServletRequest request;    
    
    @GET
    @Produces("application/json")
    public String getAll() {
        try {
            Gson gson = new Gson();
            
            List allRecords = SessionUtils.getSearchFrom(request).getSearchResult().getResults();
            return gson.toJson(allRecords);
        } catch (Exception e) {
            return "{\"error\":{\"message\":\"Error fetching records\", \"reason\":\"" + e.getMessage() + "\"}}";
        }
    }

    @Path("{id}")
    @GET
    @Produces("application/json")
    public String getRecord(@PathParam("id") String recordId) {
        try {
            Gson gson = new Gson();
            PrimoResult primoResult = PrimoPnxHelper.getRecordDoc(recordId);
            RecordDocDTO binaryRecord = new RecordDocDTO(request, primoResult, 0);
            return gson.toJson(binaryRecord);
        } catch (Exception e) {
            return "{\"error\":{\"message\":\"Error fetching records\", \"reason\":\"" + e.getMessage() + "\"}}";
        }
    }

    //jQuery.get('http://primotest.libis.be/primo_library/libweb/rest/records/pnx/LBS01003245575', function(data){console.log(data);}, 'xml');
    @Path("pnx/{id}")
    @GET
    @Produces("application/xml")
    public String getRecordPnx(@PathParam("id") String recordID, @QueryParam("encode") String recordIDEncoding) {
        try {  
            if (recordIDEncoding != null && recordIDEncoding.equals("b64")){
                recordID = Base64.base64Decode(recordID);     
            }
            
            return new RecordResourceHelper().getPNXRecordByID(recordID, request);
        } catch (Exception e) {
            return "<error><message>Error fetching record</message><reason>" + e.getMessage() + "</reason></error>";
        }
    }

    //jQuery.get('http://primotest.libis.be/primo_library/libweb/rest/records/xml/LBS01003245575', function(data){console.log(data);}, 'xml');
    @Path("xml/{id}")
    @GET
    @Produces("application/xml")
    public String getRecordXml(@PathParam("id") String recordID, @QueryParam("encode") String recordIDEncoding) {
        try {
            if (recordIDEncoding != null && recordIDEncoding.equals("b64")){
                recordID = Base64.base64Decode(recordID);                
            }
            
            return new RecordResourceHelper().getOriginalRecord(recordID);
        } catch (Exception e) {
            return "<error><message>Error fetching record</message><reason>" + e.getMessage() + "</reason></error>";
        }
    }

    //jQuery.get('http://primotest.libis.be/primo_library/libweb/rest/records/ris/LBS01003245575', function(data){console.log(data);}, 'html');
    @Path("ris/{id}")
    @GET
    @Produces("application/text")
    public Response getRecordRis(@PathParam("id") String recordID, @QueryParam("encode") String recordIDEncoding) throws IOException {
        try {
            if (recordIDEncoding != null && recordIDEncoding.equals("b64")){
                recordID = Base64.base64Decode(recordID);                
            }
                                                
            RecordDocDTO binaryRecord = new RecordResourceHelper().getRecordDocDTO(recordID, request);

            if (binaryRecord == null) {
                throw new Exception("Not found");
            }
            Date now = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyMMddhhmmss");

            return Response.ok(Ris2.fromRecordDocDTO(binaryRecord)).header("Content-Disposition", "attachment; filename=\"" + df.format(now) + ".ris\"").build();            
        } catch (Exception e) {
            Logger.getLogger(RecordResource.class.getName()).log(Level.INFO, null, e);
            return Response.serverError().type(MediaType.TEXT_PLAIN_TYPE).entity("Error fetching record:" + e.getMessage()).build();
        }
    }
    
    @Path("deeplink/{id}")
    @GET
    @Produces("application/text")
    public Response getRecordDeeplink(@PathParam("id") String recordID) {
        try {
            return Response.ok(SessionUtils.getDeepLinkForPNX(request, recordID)).build();
        } catch (Exception e) {
            Logger.getLogger(RecordResource.class.getName()).log(Level.INFO, null, e);
            return Response.serverError().type(MediaType.TEXT_PLAIN_TYPE).entity("Error fetching DeepLink:" + e.getMessage()).build();            
        }
    }
}

