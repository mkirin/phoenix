package com.dianping.phoenix.service.visitor;

import java.util.Map;

import com.dianping.phoenix.agent.resource.entity.Host;
import com.dianping.phoenix.device.IVisitor;
import com.dianping.phoenix.device.entity.Attribute;
import com.dianping.phoenix.device.entity.Device;
import com.dianping.phoenix.device.entity.Facet;
import com.dianping.phoenix.device.entity.Responce;
import com.dianping.phoenix.device.entity.Value;

public class DeviceVisitor implements IVisitor {
	public static final String KEY_OWNER = "rd_duty";

	public static final String KEY_IP = "private_ip";

	public static final String KEY_STATUS = "status";

	public static final String KEY_ENV = "env";

	public static final String KEY_HOSTNAME = "hostname";

	public static final String KEY_CATALOG = "catalog";

	public static final String KEY_APP = "app";

	private Host m_host;

	public DeviceVisitor setHost(Host host) {
		m_host = host;
		return this;
	}

	@Override
	public void visitAttribute(Attribute attribute) {
	}

	@Override
	public void visitDevice(Device device) {
		if (m_host != null && device.getAttributes() != null) {
			Map<String, Attribute> map = device.getAttributes();
			Attribute attr;
			attr = map.get(KEY_IP);
			m_host.setIp(attr == null ? "" : attr.getText());
			attr = map.get(KEY_ENV);
			m_host.setEnv(attr == null ? "" : attr.getText());
			attr = map.get(KEY_OWNER);
			m_host.setOwner(attr == null ? "" : attr.getText());
			attr = map.get(KEY_STATUS);
			m_host.setStatus(attr == null ? "" : attr.getText());
			attr = map.get(KEY_HOSTNAME);
			m_host.setHostname(attr == null ? "" : attr.getText());
		}
	}
	@Override
	public void visitFacet(Facet facet) {
	}

	@Override
	public void visitResponce(Responce responce) {
	}

	@Override
	public void visitValue(Value value) {
	}
}
