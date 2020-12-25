import io.netty.buffer.ByteBuf;
import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.channel.socket.nio.*;
import io.netty.handler.logging.*;
import io.netty.handler.codec.string.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.*;

public class NettyServer
{
    static final int PORT = Integer.parseInt(System.getProperty("port", "1234"));
  
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final NettyServerHandler serverHandler = new NettyServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder(), new StringEncoder(),
                                      new NettyServerHandler());
                        }
                    });
  
            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();
  
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
