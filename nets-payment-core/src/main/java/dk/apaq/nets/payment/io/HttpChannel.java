package dk.apaq.nets.payment.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Channel that coomunicates via Http.
 */
public class HttpChannel extends AbstractChannel {

    private static final Logger LOG = LoggerFactory.getLogger(HttpChannel.class);
    private static final int CORRECT_STATUS = 200;
    private final HttpClient client;
    private final URL url;
    private final ChannelLogger channelLogger;

    /**
     * Creates a new instance.
     *
     * @param messageFactory The message factory.
     * @param client The HttpClient
     * @param url The url
     */
    public HttpChannel(MessageFactory messageFactory, HttpClient client, URL url) {
        this(null, messageFactory, client, url);
    }

    /**
     * Creates a new instance.
     *
     * @param channelLogger
     * @param messageFactory The message factory.
     * @param client The HttpClient
     * @param url The url
     */
    public HttpChannel(ChannelLogger channelLogger, MessageFactory messageFactory, HttpClient client, URL url) {
        super(messageFactory);
        this.client = client;
        this.url = url;
        this.channelLogger = channelLogger;
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        LOG.debug("Sending message via HttpChannel [message={}]", message);
        byte[] msgData = messageToByteArray(message);

        if (channelLogger != null) {
            LOG.debug("Logging data to send.");
            channelLogger.onMessageSent(msgData);
        }

        HttpHost host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpPost postMethod = new HttpPost(url.getPath());
        ByteArrayEntity entity = new ByteArrayEntity(msgData);
        postMethod.setEntity(entity);
        HttpResponse response = client.execute(host, postMethod);

        if (response.getStatusLine().getStatusCode() != CORRECT_STATUS) {
            throw new IOException("The status code from the server was not ok. " + response.getStatusLine().getReasonPhrase());
        }

        if (response.getEntity() == null) {
            throw new IOException("The message contained in the response could not be read.");
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IOUtils.copy(response.getEntity().getContent(), buf);
        buf.flush();

        byte[] responseData = buf.toByteArray();

        if (channelLogger != null) {
            LOG.debug("Logging data recieved.");
            channelLogger.onMessageRecieved(responseData);
        }

        message = byteArrayToMessage(responseData);
        if (message == null) {
            throw new IOException("The message contained in the response could not be parsed. View log for more details.");
        }
        return message;
    }
}
