Installing brief
================
* copy lib/gson-x.x.x.jar and lib/jersey-bundle-x.x.jar into the WEB-INF/lib directory to the frontend servers
* copy bin/PrimoRestAPI.jar into the WEB-INF/lib directory on the server
* register a '/rest' endpoint in the WEB-INF/web.xml file
* restart FE and you are done

Installing
==========
* jaxrpc.jar and xmlbeans-qname.jar conflict with jBoss version and need to be disabled.
```
cd /exlibris/primo/p4_1/ng/primo/home/system/thirdparty/jbossas/server/search/deploy/jaguar-web.ear/lib
mv xmlbeans-qname.jar xmlbeans-qname.jar.conflict
mv jaxrpc.jar jaxrpc.jar.conflict 
cd /exlibris/primo/p4_1/ng/primo/home/system/thirdparty/jbossas/server/search/lib
mv xmlbeans-qname.jar xmlbeans-qname.jar.conflict
mv jaxrpc.jar jaxrpc.jar.conflict
```

* copying libraries onto the frontend servers
```
scp ./bin/PrimoRestAPI.jar ./lib/*.jar primo@your_primo_server:/tmp
ssh primo@your_primo_server
fe_web
cd WEB-INF/lib
cp /tmp/*.jar ./
```	
* registering Rest API with Primo
```	
fe_web
vi WEB-INF/web.xml
```	 
 <p>add snippet below to web.xml just above the closing </web-app> tag</p>
```
<code>
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
</code>
```	 

* restart frontend
```
fe_stop
fe_start
```	 
* test
