/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.dev.ops.common.domain;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.helpers.LogLog;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;

import com.dev.ops.exception.manager.constants.LoggingConstants;

public class ContextInfo {
	//Constants for defining toString variables.
	private static final String MODULE_NAME = "moduleName";
	private static final String OPERATION = "operation";
	private static final String SSO_TICKET = "ssoTicket";
	private static final String REQUEST_ID = "requestId";
	private static final String SESSION_ID = "sessionId";
	private static final String DEVICE_ID = "deviceId";
	private static final String TRANSACTION_ID = "transactionId";
	private static final String TRANSACTION_REQUESTED_BY_USER_ID = "transactionRequestedByUserId";
	private static final String TRANSACTION_REQUESTED_BY_USERNAME = "transactionRequestedByUsername";
	private static final String EVENT_DATA = "eventData";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private String moduleName;
	private String operation;
	private String ssoTicket;
	private String requestId;
	private String sessionId;
	private String deviceId;
	private String transactionId;
	private String transactionRequestedByUserId;
	private String transactionRequestedByUsername;
	private final JSONObject eventData;

	public ContextInfo() {
		this.eventData = new JSONObject();
		this.transactionId = this.generateTransactionId();
	}

	public ContextInfo(final String moduleName, final String operation) {
		this();
		this.setModuleName(moduleName);
		this.setOperation(operation);
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(final String moduleName) {
		this.moduleName = moduleName;
	}

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(final String operation) {
		this.operation = operation;
	}

	public String getSsoTicket() {
		return this.ssoTicket;
	}

	public void setSsoTicket(final String ssoTicket) {
		this.ssoTicket = ssoTicket;
	}

	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(final String requestId) {
		this.requestId = requestId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;
	}

	private String generateTransactionId() {
		String hostName = StringUtils.EMPTY;

		try {
			hostName = Inet4Address.getLocalHost().getHostName();
		} catch(final UnknownHostException exception) {
			LogLog.debug("Get Host Name Failed", exception);
		}

		final UUID uuid = UUID.randomUUID();

		final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		String encodedId = new String(Base64.encodeBase64(buffer.array()));

		if(StringUtils.endsWith(encodedId, LoggingConstants.WhitespaceLiterals.EQUAL_TO)) {
			encodedId = StringUtils.removeEnd(encodedId, LoggingConstants.WhitespaceLiterals.EQUAL_TO);
		}
		final String[] transactionIds = {hostName, LoggingConstants.WhitespaceLiterals.HYPHEN, encodedId};
		return StringUtils.join(transactionIds);
	}

	public String getTransactionRequestedByUserId() {
		return this.transactionRequestedByUserId;
	}

	public void setTransactionRequestedByUserId(final String transactionRequestedByUserId) {
		this.transactionRequestedByUserId = transactionRequestedByUserId;
	}

	public String getTransactionRequestedByUsername() {
		return this.transactionRequestedByUsername;
	}

	public void setTransactionRequestedByUsername(final String transactionRequestedByUsername) {
		this.transactionRequestedByUsername = transactionRequestedByUsername;
	}

	public JSONObject getEventData() {
		return this.eventData;
	}

	@SuppressWarnings("unchecked")
	public void addEventData(final String key, final String value) {
		this.eventData.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public void addAllEventData(final Map<?, ?> eventData) {
		this.eventData.putAll(eventData);
	}

	//TODO: Use JAXB for this marshalling and unmarshalling.
	public static ContextInfo toContextInfo(final String stringContextInfo) {
		final ContextInfo contextInfo = new ContextInfo();
		contextInfo.setModuleName(StringUtils.substringBetween(stringContextInfo, MODULE_NAME + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setOperation(StringUtils.substringBetween(stringContextInfo, OPERATION + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setSsoTicket(StringUtils.substringBetween(stringContextInfo, SSO_TICKET + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setRequestId(StringUtils.substringBetween(stringContextInfo, REQUEST_ID + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setSessionId(StringUtils.substringBetween(stringContextInfo, SESSION_ID + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setDeviceId(StringUtils.substringBetween(stringContextInfo, DEVICE_ID + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));

		final String transactionId = StringUtils.substringBetween(stringContextInfo, TRANSACTION_ID + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		if(StringUtils.isNotEmpty(transactionId)) {
			contextInfo.setTransactionId(transactionId);
		}

		try {
			final String eventData = StringUtils.substringBetween(stringContextInfo, EVENT_DATA + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
			contextInfo.addAllEventData(OBJECT_MAPPER.readValue(eventData, Map.class));
		} catch(final Exception e) {
			LogLog.debug("Event data cannot be converted to Map", e);
		}

		contextInfo.setTransactionRequestedByUserId(StringUtils.substringBetween(stringContextInfo, TRANSACTION_REQUESTED_BY_USER_ID + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR));
		contextInfo.setTransactionRequestedByUsername(StringUtils.substringBetween(stringContextInfo, TRANSACTION_REQUESTED_BY_USERNAME + LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR, LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR.trim()));
		return contextInfo;
	}

	@Override
	public String toString() {
		final StringBuilder stringContextInfo = new StringBuilder();
		if(this.getModuleName() != null) {
			stringContextInfo.append(MODULE_NAME).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getModuleName()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getOperation() != null) {
			stringContextInfo.append(OPERATION).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getOperation()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getSsoTicket() != null) {
			stringContextInfo.append(SSO_TICKET).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getSsoTicket()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getRequestId() != null) {
			stringContextInfo.append(REQUEST_ID).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getRequestId()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getSessionId() != null) {
			stringContextInfo.append(SESSION_ID).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getSessionId()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getDeviceId() != null) {
			stringContextInfo.append(DEVICE_ID).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getDeviceId()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getTransactionId() != null) {
			stringContextInfo.append(TRANSACTION_ID).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getTransactionId()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getTransactionRequestedByUserId() != null) {
			stringContextInfo.append(TRANSACTION_REQUESTED_BY_USER_ID).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getTransactionRequestedByUserId()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(this.getTransactionRequestedByUsername() != null) {
			stringContextInfo.append(TRANSACTION_REQUESTED_BY_USERNAME).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getTransactionRequestedByUsername()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		if(!this.getEventData().isEmpty()) {
			stringContextInfo.append(EVENT_DATA).append(LoggingConstants.WhitespaceLiterals.NAME_VALUE_SEPERATOR).append(this.getEventData()).append(LoggingConstants.WhitespaceLiterals.ATTRIBUTES_SEPERATOR);
		}
		return stringContextInfo.toString();
	}
}