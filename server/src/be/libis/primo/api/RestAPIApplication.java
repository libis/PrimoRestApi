/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api;

import be.libis.primo.api.resource.RecordResource;
import be.libis.primo.api.resource.SessionResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author mehmetc
 */
  
public class RestAPIApplication extends Application {
    @Override
       public Set<Class<?>> getClasses() {
           Set<Class<?>> s = new HashSet<Class<?>>();   
           s.add(SessionResource.class);
           s.add(RecordResource.class);
           return s;
       }          
}
