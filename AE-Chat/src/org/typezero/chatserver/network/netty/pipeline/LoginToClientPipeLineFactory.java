/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 * <p/>
 * Aion-Lightning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * Aion-Lightning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. *
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Aion-Lightning.
 * If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * <p/>
 * Credits goes to all Open Source Core Developer Groups listed below
 * Please do not change here something, ragarding the developer credits, except the "developed by XXXX".
 * Even if you edit a lot of files in this source, you still have no rights to call it as "your Core".
 * Everybody knows that this Emulator Core was developed by Aion Lightning
 *
 * @-Aion-Unique-
 * @-Aion-Lightning
 * @Aion-Engine
 * @Aion-Extreme
 * @Aion-NextGen
 * @Aion-Core Dev.
 */
package org.typezero.chatserver.network.netty.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.typezero.chatserver.network.aion.ClientPacketHandler;
import org.typezero.chatserver.network.netty.coder.LoginPacketDecoder;
import org.typezero.chatserver.network.netty.coder.LoginPacketEncoder;
import org.typezero.chatserver.network.netty.coder.PacketFrameDecoder;
import org.typezero.chatserver.network.netty.handler.ClientChannelHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ATracer
 */
public class LoginToClientPipeLineFactory implements ChannelPipelineFactory {

	private static final int THREADS_MAX = 10;
	private static final int MEMORY_PER_CHANNEL = 1048576;
	private static final int TOTAL_MEMORY = 134217728;
	private static final int TIMEOUT = 100;
	private final ClientPacketHandler clientPacketHandler;
	private ExecutionHandler executionHandler;

	public LoginToClientPipeLineFactory(ClientPacketHandler clientPacketHandler) {
		this.clientPacketHandler = clientPacketHandler;
		this.executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(THREADS_MAX,
				MEMORY_PER_CHANNEL, TOTAL_MEMORY, TIMEOUT, TimeUnit.MILLISECONDS, Executors.defaultThreadFactory()));
	}

	/**
	 * Decoding process will include the following handlers: - framedecoder -
	 * packetdecoder - handler Encoding process: - packetencoder Please note the
	 * sequence of handlers
	 */
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

		pipeline.addLast("framedecoder", new PacketFrameDecoder());
		pipeline.addLast("packetdecoder", new LoginPacketDecoder());
		pipeline.addLast("packetencoder", new LoginPacketEncoder());
		pipeline.addLast("executor", executionHandler);
		pipeline.addLast("handler", new ClientChannelHandler(clientPacketHandler));

		return pipeline;
	}
}