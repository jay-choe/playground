package selector;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import selector.common.Config;

public class SelectorClient {

    private Selector selector;

    public SelectorClient() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(Config.LOOP_BACK_ADDR, Config.PORT));
            sc.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();  // declare for saying done event processing
                if (selectionKey.isConnectable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
//                    if (channel.isConnectionPending()) channel.finishConnect();
                    channel.configureBlocking(false);
                    System.out.println("Connected To Server");
                    channel.write(ByteBuffer.wrap("Hello From Client".getBytes(StandardCharsets.UTF_8)));
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(Config.BUFFER_SIZE);
                    // For simple demo not consider long bytes
                    channel.read(buffer);
                    String receiveData = new String(buffer.array())
                        .trim();
                    System.out.println("Received Data: " + receiveData);
                    buffer.compact();
                }
            }
        }
    }

    private void clear(List<? extends Closeable> toCloseList)
        throws IOException {
        for (Closeable list : toCloseList)
            list.close();
    }

    public static void main(String[] args) throws IOException {
        SelectorClient selectorClient = new SelectorClient();
        selectorClient.init();
        selectorClient.connect();
    }
}
