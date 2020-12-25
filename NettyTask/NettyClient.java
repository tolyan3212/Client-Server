import java.io.*;
import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.channel.socket.nio.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.*;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.*;

public final class NettyClient {
  
    static final String HOST = "localhost";
    static final int PORT = 1234;
  
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("framer",
                                             new DelimiterBasedFrameDecoder
                                             (1234, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder",
                                             new StringDecoder());
                            pipeline.addLast("encoder",
                                             new StringEncoder());
                            pipeline.addLast("handler",
                                             new NettyClientHandler());

                        }                
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            Channel channel = f.channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                ChannelFuture cf = channel.writeAndFlush(line + "\r\n");
            }
            
        } finally {
            group.shutdownGracefully();
        }
    }
}
