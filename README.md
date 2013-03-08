PrimoRestApi
============


PRIMO 4.0 cleanup conflicts
  cd /exlibris/primo/p4_1/ng/primo/home/system/thirdparty/jbossas/server/search/deploy/jaguar-web.ear/lib
  mv xmlbeans-qname.jar xmlbeans-qname.jar.conflict
  mv jaxrpc.jar jaxrpc.jar.conflict

  cd /exlibris/primo/p4_1/ng/primo/home/system/thirdparty/jbossas/server/search/lib
  mv xmlbeans-qname.jar xmlbeans-qname.jar.conflict
  mv jaxrpc.jar jaxrpc.jar.conflict


Patch web.xml to listen for requests on /rest
/exlibris/primo/p4_1/ng/primo/home/system/thirdparty/jbossas/server/search/deploy/primo_library-app.ear/primo_library-libweb.war/WEB-INF/web.xml
    <servlet>
        <servlet-name>PrimoRestApi</servlet-name>
        <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
        <init-param>
                <param-name>javax.ws.rs.Application</param-name>
                <param-value>be.libis.primo.api.RestAPIApplication</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
       <servlet-name>PrimoRestApi</servlet-name>
       <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>
