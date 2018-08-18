package ruk.writability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by rukshani on 8/18/18.
 */
public class NioClientWithSelector {
    public void startClient() throws IOException, InterruptedException {

        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 9093);
        SocketChannel client = SocketChannel.open(hostAddress);

        String threadName = Thread.currentThread().getName();

        //If socket is configured to be non blocking and if the server delays its response, the number of
        //bytes read will be 0. Only if it is blocking the read will wait for the server to send data. And
        //when its blocking and the server doesn't send anything it waits forever.
       /* System.out.println(threadName + " started");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
      //  client.configureBlocking(false);
        int noOfBytesRead = client.read(buffer);
        System.out.println("No of bytes read by client : " + noOfBytesRead);
        byte[] data = new byte[noOfBytesRead];
        System.arraycopy(buffer.array(), 0, data, 0, noOfBytesRead);
        System.out.println("Got from server : " + new String(data));
        client.close();*/

       //Using selector for client side
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.configureBlocking(false);
        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);

        while (true) {
            // wait for events
            int readyCount = selector.select();

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
                if (key.isReadable()) { // Read from client
                    this.read(key);
                } else if (key.isWritable()) {
                    //
                }
            }
        }
    }

    public static void main(String[] args) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                try {
                    new NioClientWithSelector().startClient();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(client, "client-A").start();
      //  new Thread(client, "client-B").start();
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
            System.out.println("Connection closed by server: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }

}
