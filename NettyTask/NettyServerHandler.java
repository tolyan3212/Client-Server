import java.util.*;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.*;
import io.netty.util.*;  

public class NettyServerHandler extends SimpleChannelInboundHandler<String>{
    String username;
    boolean registered = false;
    static final Set<String> users = new TreeSet<String>();
    static final List<String> messages = new ArrayList<String>();
    static final List<Channel> channels = new ArrayList<Channel>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String s) {
        s = s.trim();
        ctx.writeAndFlush(getResponse(s) + "\r\n");
    }
  
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String getResponse(String request) {
        try {
            String line1 = "";
            String line2 = "";
            char o = '+';
            String operations = "+-*/";
            for (int i = 0; i < request.length(); i++) {
                if (operations.indexOf(request.charAt(i)) == -1) {
                    if (request.charAt(i) != ' ')
                        line1 += request.charAt(i);
                }
                else {
                    o = request.charAt(i);
                    for (int j  = i+1; j < request.length(); j++) {
                        if (request.charAt(j) != ' ')
                            line2 += request.charAt(j);
                    }
                    break;
                }
            }
            int a = Integer.parseInt(line1);
            int b = Integer.parseInt(line2);
            int res = 0;
            if (o == '+') res = a + b;
            else if (o == '-') res = a-b;
            else if (o == '*') res = a*b;
            else if (o == '/') res = a/b;
            return Integer.toString(res);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }
}
