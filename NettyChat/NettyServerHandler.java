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
        channels.add(ctx.channel());
        ChannelFuture cf = ctx.writeAndFlush("Enter username:\r\n");
        if (!cf.isSuccess()) {
            System.out.println("failed: " + cf.cause());
        }
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String s) {
        s = s.trim();
        System.out.println("received: " + s);
        if (!registered) {
            if (users.contains(s)) {
                ctx.writeAndFlush("This name is already taken, choose another one:\r\n");
            }
            else {
                users.add(s);
                registered = true;
                username = s;
                for (String m : messages) {
                    ctx.writeAndFlush(m + "\r\n");
                }
                sendToAllClients(s + " is in the chat!\r\n");
            }
        }
        else {
            String m = "[" + username + "]:" + s;
            messages.add(m);
            sendToAllClients(m + "\r\n");
        }
    }
  
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        users.remove(username);
        channels.remove(ctx.channel());
        sendToAllClients("User " + username
                         + " left the chat\r\n");
    }
  
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendToAllClients(String message) {
        for (Channel c : channels) {
            c.writeAndFlush(message);
        }
    }

}
