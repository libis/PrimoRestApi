/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.libis.primo.api.resource;

import be.libis.primo.api.resource.helper.SessionParameters;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 *
 * @author mehmetc
 */
@Path("/session")
public class SessionResource {
    @Context HttpServletRequest request;
    
    @GET
    @Produces("application/json")
    public String getSession() {
        Gson gson = new Gson();
        return gson.toJson(new SessionParameters(request));
    }    
}
