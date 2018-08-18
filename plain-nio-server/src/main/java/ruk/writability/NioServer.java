package ruk.writability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by rukshani on 8/18/18.
 */
public class NioServer {
    private Selector selector;

    private InetSocketAddress listenAddress;
    private final static int PORT = 9093;

    public static void main(String[] args) throws Exception {
        try {
            new NioServer("localhost", 9093).startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NioServer(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, PORT);
    }

    /**
     * Start the server
     *
     * @throws IOException
     */
    private void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port >> " + PORT);

        while (true) {
            // wait for events
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }

            // process selected keys...
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                // Remove key from set so we don't process it twice
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) { // Accept client connections
                    this.accept(key);
                } else if (key.isReadable()) { // Read from client
                    this.read(key);
                } else if (key.isWritable()) {
                    this.write(key);
                }
            }
        }
    }

    // accept client connection
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

		/*
         * Register channel with selector for further IO (record it for read/write
		 * operations)
		 */
        channel.register(this.selector, SelectionKey.OP_READ);
        channel.register(this.selector, SelectionKey.OP_WRITE);
    }

    // read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }

    // write to socket channel
    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(74);
        buffer.put("Sent from server".getBytes());
        buffer.flip();

        channel.write(buffer);
        //Once everything is written clear the OP_WRITE
          try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int interestOps = key.interestOps();
        if ((interestOps & SelectionKey.OP_WRITE) != 0) {
            key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
        }
    }
}
