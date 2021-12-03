package com.miaosha.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

// 当spring容器中没有TomcatEmbeddedServletContainerFactory这个bean时，
// 会把这个bean加载进来
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        // 使用对应工厂类提供的接口定制化tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

                //定制化keepalivetimeout
                protocol.setKeepAliveTimeout(300000);
                //当客户端发送超过10000个请求断开keepalive连接
                protocol.setMaxKeepAliveRequests(10000);

            }
        });
    }
}
