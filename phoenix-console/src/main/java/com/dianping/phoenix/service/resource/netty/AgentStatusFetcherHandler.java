package com.dianping.phoenix.service.resource.netty;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import com.dianping.phoenix.agent.resource.entity.Host;
import com.dianping.phoenix.agent.response.entity.Response;
import com.dianping.phoenix.agent.response.transform.DefaultJsonParser;
import com.dianping.phoenix.service.visitor.AgentResponseVisitor;

public class AgentStatusFetcherHandler extends SimpleChannelUpstreamHandler {
	private CountDownLatch m_latch;

	private Map<String, Host> m_hosts;

	private StringBuilder m_response;

	private boolean m_readingChunks;

	public AgentStatusFetcherHandler(CountDownLatch latch, List<Host> hosts) {
		m_latch = latch;
		m_hosts = new HashMap<String, Host>();
		for (Host host : hosts) {
			m_hosts.put(host.getIp(), host);
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		if (!m_readingChunks) {
			HttpResponse response = (HttpResponse) event.getMessage();
			m_response = new StringBuilder();

			if (response.isChunked()) {
				m_readingChunks = true;
			} else {
				ChannelBuffer content = response.getContent();

				if (content.readable()) {
					m_response.append(content.toString(CharsetUtil.UTF_8));
					fillHostWithResponse();
				}

				m_latch.countDown();
			}
		} else {
			HttpChunk chunk = (HttpChunk) event.getMessage();
			if (chunk.isLast()) {
				fillHostWithResponse();
				m_readingChunks = false;
				m_latch.countDown();
			} else {
				m_response.append(chunk.getContent().toString(CharsetUtil.UTF_8));
			}
		}
	}

	private void fillHostWithResponse() {
		try {
			Response res = DefaultJsonParser.parse(m_response.toString());
			if (res != null) {
				Host host = m_hosts.get(res.getIp());
				if (host != null) {
					res.accept(new AgentResponseVisitor(host));
				}
			}
		} catch (IOException e) {
			// ignore it
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		ctx.getChannel().close();
		m_latch.countDown();
	}
}
