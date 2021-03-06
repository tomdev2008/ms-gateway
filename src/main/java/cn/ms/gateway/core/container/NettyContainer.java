package cn.ms.gateway.core.container;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.GenericFutureListener;
import cn.ms.gateway.base.container.IContainer;
import cn.ms.gateway.base.filter.IFilterFactory;
import cn.ms.gateway.common.Conf;
import cn.ms.gateway.common.log.Logger;
import cn.ms.gateway.common.log.LoggerFactory;
import cn.ms.gateway.common.thread.NamedThreadFactory;
import cn.ms.gateway.entity.GatewayREQ;
import cn.ms.gateway.entity.GatewayRES;

/**
 * 基于Netty实现微服务网关容器
 * 
 * @author lry
 */
public class NettyContainer implements IContainer<GatewayREQ, GatewayRES> {
	
	private static final Logger logger=LoggerFactory.getLogger(NettyContainer.class);
	
	EventLoopGroup bossGroup = null;
	EventLoopGroup workerGroup = null;
	ServerBootstrap serverBootstrap = null;
	IFilterFactory<GatewayREQ, GatewayRES> filterFactory;
	
	public NettyContainer(IFilterFactory<GatewayREQ, GatewayRES> filterFactory) {
		this.filterFactory = filterFactory;
	}

	@Override
	public void init() throws Exception {
		this.bossGroup = new NioEventLoopGroup(Conf.CONF.getBossGroupThread(), new NamedThreadFactory("NettyContainerBoss"));
		this.workerGroup = new NioEventLoopGroup(Conf.CONF.getWorkerGroupThread(), new NamedThreadFactory("NettyContainerWorker"));

		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new HttpResponseEncoder());
						ch.pipeline().addLast(new HttpRequestDecoder());
						ch.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
						ch.pipeline().addLast(new NettyContainerHandler(filterFactory));
					}
				}).option(ChannelOption.SO_BACKLOG, 1024)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
	}

	@Override
	public void start() throws Exception {
		ChannelFuture channelFuture = serverBootstrap.bind(Conf.CONF.getPort()).sync();
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()){
					logger.info("启动成功: http://%s:%s/", GatewayREQ.LOACALHOST, Conf.CONF.getPort());
				}else{
					logger.error("启动失败");
				}
			}
		});
	}

	@Override
	public void shutdown() throws Exception {
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}

		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		
		if (serverBootstrap != null) {
			serverBootstrap.clone();
		}
	}

}
