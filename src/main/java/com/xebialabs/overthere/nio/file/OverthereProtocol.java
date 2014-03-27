package com.xebialabs.overthere.nio.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum OverthereProtocol {
	LOCAL("local", false),
	SSH_SCP("ssh+scp", true),
	SSH_SFTP("ssh+sftp", true),
	SSH_SUDO("ssh+sudo", true),
	SSH_INTERACTIVE_SUDO("ssh+interactive-sudo", true),
	CIFS_TELNET("cifs+telnet", false),
	CIFS_WINRM("cifs+winrm", false);
	
	private String uriProtocol;
	private boolean isTestable;
	
	private OverthereProtocol(String uriProtocol, boolean isTestable) {
		this.uriProtocol = uriProtocol;
		this.isTestable = isTestable;
	}
	
	public String forUri() {
		return uriProtocol;
	
	}
	
	public static List<OverthereProtocol> valuesList() {
		return Arrays.asList(values());
	}
	
	public static List<OverthereProtocol> testableValuesList() {
		List<OverthereProtocol> output = new ArrayList<OverthereProtocol>();
		
		for (OverthereProtocol protocol : values()) {
			if (protocol.isTestable) {
				output.add(protocol);
			}
		}
		
		return output;
	}
}
