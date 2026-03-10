package org.ceciliastudio.logbridgeagent;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[GameStub LogBridge] Premain loaded. agentArgs: " + agentArgs);
        Path socketPath = Path.of(agentArgs);
        if (!Files.exists(socketPath)) {
            System.err.println("[GameStub LogBridge] the socket file does not exists: " + socketPath);
            System.exit(40);
        }

        try {
            SocketChannel socket = createSocket(socketPath);
            System.setOut(new PrintStream(new BridgedOutputStream(socket, System.out, false)));
            System.setErr(new PrintStream(new BridgedOutputStream(socket, System.err, true)));
        } catch (IOException e) {
            System.err.println("[GameStub LogBridge] Failed to create socket: " + e.getMessage());
        }
    }

    private static SocketChannel createSocket(Path path) throws IOException {
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(path);
        SocketChannel socketChannel = SocketChannel.open(StandardProtocolFamily.UNIX);
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        return socketChannel;
    }
}
