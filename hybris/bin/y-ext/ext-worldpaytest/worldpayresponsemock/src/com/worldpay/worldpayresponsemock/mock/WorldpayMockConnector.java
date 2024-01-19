package com.worldpay.worldpayresponsemock.mock;


import com.worldpay.exception.WorldpayException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.conn.socket.PlainConnectionSocketFactory.getSocketFactory;

/**
 * Mocked connector to Worldpay
 */
public class WorldpayMockConnector {

    private static final Logger LOG = Logger.getLogger(WorldpayMockConnector.class);

    private static final String WORLDPAYRESPONSEMOCK_ORDER_NOTIFICATION_ENDPOINT = "worldpayresponsemock.order.notification.endpoint";
    private static final String NOTIFICATION_EXTENSION_CONTEXT_ROOT = "worldpaynotifications.webroot";
    private static final String SCHEME_SEPARATOR = "://";
    private static final String PROTOCOL_SEPARATOR = ":";
    private static final String EXCEPTION_MESSAGE = "Exception sending response using the mocked connector";

    private RestTemplate worldpayRestTemplate;
    private ConfigurationService configurationService;

    /**
     * Send the response
     *
     * @param request
     * @param responseXML
     * @throws WorldpayException
     */
    public void sendResponse(final HttpServletRequest request, final String responseXML) throws WorldpayException {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpClient;
        try {
            httpClient = buildAllowingAllHostNamesHttpClient();
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            LOG.error(EXCEPTION_MESSAGE, e);
            throw new WorldpayException(EXCEPTION_MESSAGE, e);
        }
        factory.setHttpClient(httpClient);
        worldpayRestTemplate.setRequestFactory(factory);
        worldpayRestTemplate.postForObject(constructEndpoint(request), responseXML, String.class);
    }

    protected HttpClient buildAllowingAllHostNamesHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, authType) -> true).build();
        httpClientBuilder.setSSLContext(sslContext);

        final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        httpClientBuilder.setSSLSocketFactory(sslConnectionFactory);

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", getSocketFactory())
                .build();

        final HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

        httpClientBuilder.setConnectionManager(ccm);

        return httpClientBuilder.build();
    }

    private String constructEndpoint(final HttpServletRequest request) {
        final String orderNotificationEndpointPostfix = configurationService.getConfiguration().getString(WORLDPAYRESPONSEMOCK_ORDER_NOTIFICATION_ENDPOINT);
        final String notificationExtensionContextRoot = configurationService.getConfiguration().getString(NOTIFICATION_EXTENSION_CONTEXT_ROOT);
        final String serverName = request.getServerName();
        final String scheme = request.getScheme();
        int serverPort = request.getServerPort();
        return scheme + SCHEME_SEPARATOR + serverName + PROTOCOL_SEPARATOR + serverPort + notificationExtensionContextRoot + orderNotificationEndpointPostfix;
    }

    @Required
    public void setWorldpayRestTemplate(RestTemplate worldpayRestTemplate) {
        this.worldpayRestTemplate = worldpayRestTemplate;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
