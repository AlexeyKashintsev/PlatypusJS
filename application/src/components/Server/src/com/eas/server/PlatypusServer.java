/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.server;

import com.eas.client.DatabasesClient;
import com.eas.client.cache.FilesAppCache;
import com.eas.client.scripts.ScriptDocument;
import com.eas.client.threetier.ErrorResponse;
import com.eas.client.threetier.Request;
import com.eas.script.JsDoc;
import com.eas.server.mina.platypus.PlatypusRequestsHandler;
import com.eas.server.mina.platypus.RequestDecoder;
import com.eas.server.mina.platypus.ResponseEncoder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author pk, mg refactoring
 */
public class PlatypusServer extends PlatypusServerCore {

    public final static int DEFAULT_PORT = 8500;
    public final static String DEFAULT_PROTOCOL = "platypus";
    public final static int DEFAULT_CLIENT_THREADS = 3;
    public final static int DEFAULT_WORKER_THREADS = 3;
    protected SSLContext sslContext;
    public final static String HTTP_PROTOCOL = "http";
    public final static String HTTPS_PROTOCOL = "https";
    public final static int DEFAULT_EXECUTOR_POOL_SIZE = 16;
    private final ExecutorService bgTasksExecutor;
    private final InetSocketAddress[] listenAddresses;
    private final Map<Integer, String> portsProtocols;
    
    public PlatypusServer(DatabasesClient aDatabasesClient, SSLContext aSslContext, InetSocketAddress[] aAddresses, Map<Integer, String> aPortsProtocols, Set<String> aTasks, String aDefaultAppElement) throws Exception {
        super(aDatabasesClient, aTasks, aDefaultAppElement);

        if (aAddresses == null) {
            throw new NullPointerException("listenAddresses");
        } else if (aAddresses.length == 0) {
            throw new IllegalArgumentException("listenAddresses is empty");
        }
        bgTasksExecutor = Executors.newCachedThreadPool();
        listenAddresses = aAddresses;
        portsProtocols = aPortsProtocols;
        sslContext = aSslContext;
    }

    public void start() throws Exception {
        Logger.getLogger(PlatypusServer.class.getName()).log(Level.INFO, "Application elements are located at: {0}", databasesClient.getAppCache() instanceof FilesAppCache ? ((FilesAppCache) databasesClient.getAppCache()).getSrcPathName() : databasesClient.getSettings().getUrl());
        startBackgroundTasks();
        for (InetSocketAddress s : listenAddresses) {
            initializeAndBindAcceptor(s);
        }
        assert listenAddresses != null : "listenAddresses != null";
        assert listenAddresses.length > 0 : "listenAddresses.length > 0";
    }

    public void stop(int awaitTimeout, TimeUnit timeUnit) {
        try {
            databasesClient.shutdown();
        } catch (Exception ex) {
            Logger.getLogger(PlatypusServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enqueuePlatypusRequest(final Request aRequest, final Runnable onFailure, final Runnable onSuccess) throws Exception {
        bgTasksExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final RequestHandler handler = RequestHandlerFactory.getHandler(PlatypusServer.this, sessionManager.getSystemSession(), aRequest);
                    handler.run();
                    if (handler.getResponse() instanceof ErrorResponse) {
                        if (onFailure != null) {
                            onFailure.run();
                        }
                    } else {
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PlatypusServer.class.getName()).log(Level.SEVERE, null, ex);
                    if (onFailure != null) {
                        onFailure.run();
                    }
                }
            }
        });
    }

    private String findAcceptorModule(String aProtocol) throws Exception {
        String lastAcceptor = null;
        for (String taskModuleId : tasks) {
            ScriptDocument sDoc = scriptDocuments.compileScriptDocument(taskModuleId);
            if (sDoc != null) {
                boolean isAcceptor = false;
                Set<String> protocols = new HashSet<>();
                for (JsDoc.Tag tag : sDoc.getModuleAnnotations()) {
                    if (JsDoc.Tag.ACCEPTOR_TAG.equals(tag.getName())) {
                        isAcceptor = true;
                    }
                    if (JsDoc.Tag.ACCEPTED_PROTOCOL_TAG.equals(tag.getName()) && tag.getParams() != null && !tag.getParams().isEmpty()) {
                        protocols.addAll(tag.getParams());
                    }
                }
                if (isAcceptor) {
                    if (!protocols.isEmpty()) {// specific protocol acceptor
                        if (protocols.contains(aProtocol)) {
                            return taskModuleId;
                        }
                    } else {
                        lastAcceptor = taskModuleId;
                    }
                }
            }
        }
        return lastAcceptor;
    }

    private void tryToInitializeAndBindSensorAcceptor(String protocol, InetSocketAddress s) throws IOException, Exception {
        Logger logger = Logger.getLogger(ServerMain.class.getName());
        if (com.eas.sensors.positioning.AcceptorsFactory.isSupported(protocol)) {
            String acceptorModuleId = findAcceptorModule(protocol);
            if (acceptorModuleId != null) {
                IoAcceptor sensorAcceptor = com.eas.sensors.positioning.AcceptorsFactory.create(protocol, new com.eas.server.handlers.PositioningPacketReciever(this, acceptorModuleId));
                if (sensorAcceptor != null) {
                    sensorAcceptor.bind(s);
                    logger.info(String.format("\nListening on %s; protocol: %s\n", s.toString(), protocol));
                }
            } else {
                logger.info(String.format("\nAcceptor server module was not found for protocol \"%s\"", protocol));
            }
        } else {
            logger.info(String.format("\nProtocol \"%s\" is not supported", protocol));
        }
    }

    private void initializeAndBindAcceptor(InetSocketAddress s) throws IOException, Exception {
        // archive protocol for address
        String protocol = DEFAULT_PROTOCOL;
        if (portsProtocols.containsKey(s.getPort())) {
            protocol = portsProtocols.get(s.getPort());
        }
        // initialize acceptor according to protocol
        if (DEFAULT_PROTOCOL.equalsIgnoreCase(protocol)) {
            initializeAndBindPlatypusAcceptor(s);
        } else {
            tryToInitializeAndBindSensorAcceptor(protocol, s);
        }
    }

    private void initializeAndBindPlatypusAcceptor(InetSocketAddress s) throws IOException, Exception {
        final SslFilter sslFilter = new SslFilter(sslContext);
        final ExecutorService executor = new OrderedThreadPoolExecutor(DEFAULT_EXECUTOR_POOL_SIZE);

        final IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("executor", new ExecutorFilter(executor, IoEventType.EXCEPTION_CAUGHT,
                IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT, IoEventType.SESSION_CLOSED));
        acceptor.getFilterChain().addLast("encryption", sslFilter);
        acceptor.getFilterChain().addLast("platypusRequestCodec", new ProtocolCodecFilter(new ResponseEncoder(), new RequestDecoder()));
        acceptor.setHandler((IoHandler) new PlatypusRequestsHandler(this));

        acceptor.bind(s);

        Logger logger = Logger.getLogger(ServerMain.class.getName());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\nListening on %s; protocol: platypus\n", s.toString()));
        if (sslFilter.getEnabledCipherSuites() != null) {
            sb.append("Enabled cipher suites: ").append(sslFilter.getEnabledCipherSuites()).append("\n");
        }
        if (sslFilter.getEnabledProtocols() != null) {
            sb.append("Enabled protocols: ").append(sslFilter.getEnabledProtocols()).append("\n");
        }
        logger.info(sb.toString());
    }
}
