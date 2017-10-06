package com.github.jsundqvist;

import com.tightvnc.vncviewer.VncSenderConnection;

public class VncAuthorizedKeys {

	private static String vncHost;
	private static int vncPort;
	private static String vncPassword;

	public static void main(String[] args) throws Exception {

		VncSenderConnection jvnc = new VncSenderConnection(vncHost, vncPort, vncPassword);
		jvnc.open();

		//jvnc.print("!\"#¤%()?`´"); //¡@£$€¥{[]}\±±åäöøæ^¨¨^~''~´´''***
		jvnc.println("mkdir -p ~/.ssh && echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCpyU76U152qouWoatF2eruBzyedX6A6J0g7Ei1yWoZnoFqkmOIX4Q66kdvrw3E0CbW/ayzoAN+eJtbCq1cHKceOBJ9wUXXZMOlPooJUEtEUJiFK5Sey1MnQVcUrjKs4WivJlvvzm2oz+wiCDhExENFbE6XE0C6Y0bruf9yz9h9fWwvbUmR/6k7TVjjZOm021nOUFEkKLcOPj4nFbuowjaBLUv0x8zUYbaYxjndSCcDWxF3zEyBat5mJpLEGWupnMVFt51ZIWZYQdojcSF9v5/Xt7KfbF2CT8yP7gs8uh0QqtDGvsSJKgyCZbOacSC2e5O4DJEQEpyqh/Lqyb707te1 root@005486b0f160' >> ~/.ssh/authorized_keys");

	}

	private static void sleep() throws InterruptedException {
		Thread.sleep(100);
	}

}
