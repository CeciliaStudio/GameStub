package org.ceciliastudio.logbridgeagent;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class BridgedOutputStream extends OutputStream {
    private final SocketChannel channel;
    private final OutputStream standardOutputStream;
    private final boolean isError;
    private final ByteBuffer buffer;

    public BridgedOutputStream(SocketChannel channel, OutputStream standardOutputStream, boolean isError) {
        this.channel = channel;
        this.standardOutputStream = standardOutputStream;
        this.isError = isError;
        this.buffer = ByteBuffer.allocate(1024);
        this.buffer.put((byte) (isError ? 0x01 : 0x00));
    }

    @Override
    public void write(int b) throws IOException {
        this.standardOutputStream.write(b);
        this.buffer.put((byte) b);
        if (b == '\n') {
            this.buffer.flip();
            this.channel.write(this.buffer);
            this.buffer.clear();
            this.buffer.put((byte) (isError ? 0x01 : 0x00));
        }
    }

    @Override
    public void close() throws IOException {
        this.standardOutputStream.close();
        this.channel.close();
    }
}
