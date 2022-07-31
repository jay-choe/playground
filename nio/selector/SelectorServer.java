package selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import selector.common.Config;

public class SelectorServer {

    private Selector selector;

    public SelectorServer() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() throws IOException {
        System.out.println("Server is listening to: " + Config.PORT);

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = channel.accept();
                    System.out.println("=======Accepted========");
                    socketChannel.write(ByteBuffer.wrap("Hello From Server"
                        .getBytes(StandardCharsets.UTF_8)));
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(Config.BUFFER_SIZE);
                    channel.read(buffer);
                    byte[] data = buffer.array();
                    String msg = new String(data).trim();
                    System.out.println(msg);
                    buffer.clear();
                }
            }
        }
    }

    private void init(int port) {
        assert port > 0 && String.valueOf(port).length() > 3;

        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.socket().bind(new InetSocketAddress(port));
            // for Non-blocking I/O
            channel.configureBlocking(false);
            channel.supportedOptions()
                .stream()
                .forEach(System.out::println);
            channel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SelectorServer server = new SelectorServer();
        server.init(Config.PORT);
        server.listen();
    }
}