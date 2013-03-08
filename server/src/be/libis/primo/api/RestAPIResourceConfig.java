/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api;

import com.sun.jersey.api.core.PackagesResourceConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author mehmetc
 */
public class RestAPIResourceConfig extends PackagesResourceConfig {

    public RestAPIResourceConfig() {
        super("be.libis.primo.api");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(be.libis.primo.api.resource.SessionResource.class);
        s.add(be.libis.primo.api.resource.RecordResource.class);
        return s;
    }

    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        Map<String, MediaType> m = new HashMap<String, MediaType>();
        m.put("json", MediaType.APPLICATION_JSON_TYPE);
        m.put("xml", MediaType.APPLICATION_XML_TYPE);
        m.put("pnx", MediaType.valueOf("application/pnx"));
        return m;
    }
}
