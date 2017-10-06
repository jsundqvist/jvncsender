package com.tightvnc.vncviewer;

/* 
* Please note:
* we use the package com.tightvnc.vncviewer because then we don't have to change the visibility
* of methods or variables
* */

import be.jedi.jvncsender.VncMappings;
import com.tigervnc.rfb.UnicodeToKeysym;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

//http://stackoverflow.com/questions/1248510/convert-string-to-keyevents
//http://stackoverflow.com/questions/664896/get-the-vk-int-from-an-arbitrary-char-in-java

// loadkeys us -> on the server does the trick -> default to US on CDBOOT

//	    Locale locale = Locale.getDefault();
//        System.out.println("Before setting, Locale is = " + locale);
//        // Setting default locale    
//  	  System.out.println(KeyEvent.VK_L);
//  	  
//        locale = new Locale("nl","BE");
//        Locale.setDefault(locale);
//        System.out.println("After setting, Locale is = " + locale);
//
//  	  System.out.println(KeyEvent.VK_L);

//  http://stackoverflow.com/questions/834758/preserving-keyboard-layout-in-a-jtextfield
// http://forums.sun.com/thread.jspa?threadID=762425	
//        InputContext context=InputContext.getInstance();
//        System.out.println(context.getLocale().getCountry());
//        
//        System.out.println(context.selectInputMethod(Locale.ENGLISH));
//        System.out.println(context.getLocale().getDisplayLanguage());

public class VncSenderConnection {

	public static final int ENTER = 0xFF0D;
	public static final int ESC = 0xFF1B;
	public static final int SHIFT = 0xFFE1;

	RfbProto rfb;
	String host = "localhost";
	int port = 5900;
	String password = "";

	boolean shift = false;
	boolean alt = false;
	boolean control = false;
	boolean meta = false;

	public VncSenderConnection(String host, int port, String password) {
		this.host = host;
		this.port = port;
		this.password = password;

	}

	public void open() throws Exception {
		this.connectAndAuthenticate();
		System.out.println("sending init string");
		this.sendInit();
	}

	public void close() throws IOException {
		this.sendClose();
	}

	public void print(String string) throws IOException {
		List<Integer> keyCodes = keySyms(string);
		write(keyCodes);
	}

	public List<Integer> keySyms(String string) {
		List<Integer> keySyms = new ArrayList<Integer>();
		for(Character character : string.toCharArray()) {
			if(character != KeyEvent.CHAR_UNDEFINED) {
				int keysym = UnicodeToKeysym.ucs2keysym(Character.toString(character).codePointAt(0));
				if(shouldShift(keysym))
					keySyms.add(SHIFT);
				keySyms.add(keysym);
			}
		}
		return keySyms;
	}

	private boolean shouldShift(int keysym) {
		return Arrays.asList(33, 34, 35, 37, 38, 40, 41, 60, 62, 63, 64, 95, 96, 126, 164, 180).contains(keysym);
	}

	public void println(String string) throws IOException {
		List<Integer> keySyms = keySyms(string);
		keySyms.add(ENTER);
		write(keySyms);
	}

	public void write(List<Integer> keyCodes) throws IOException {
		for(Integer keyCode : keyCodes) {
			write(keyCode);
			sleep(1);
		}
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void write(int keyCode) throws IOException {
		switch(keyCode) {
			case 0xffe1:
				shift = true;
				rfb.writeKeyEvent(keyCode, true);
				break;
			case 0xffe9:
				alt = true;
				rfb.writeKeyEvent(keyCode, true);
				break;
			case 0xffe3:
				control = true;
				rfb.writeKeyEvent(keyCode, true);
				break;
			case 0xffe7:
				meta = true;
				rfb.writeKeyEvent(keyCode, true);
				break;
			default: {

				//Key Press
				rfb.writeKeyEvent(keyCode, true);
				//Key Release
				rfb.writeKeyEvent(keyCode, false);

				// Reset modifiers after
				if(shift) {
					shift = false;
					rfb.writeKeyEvent(0xffe1, false);
				}
				if(alt) {
					alt = false;
					rfb.writeKeyEvent(0xffe9, false);
				}
				if(control) {
					rfb.writeKeyEvent(0xffe3, false);
					control = false;
				}
				if(meta) {
					rfb.writeKeyEvent(0xffe7, false);
					meta = false;
				}
				rfb.os.write(rfb.eventBuf, 0, rfb.eventBufLen);
				//resetting the buffer
				rfb.eventBufLen = 0;
			}

		}
	}

	void sendInit() throws Exception {
		rfb.os.write(0);

	}

	void sendTest() throws Exception {

		rfb.eventBufLen = 0;

		rfb.writeKeyEvent(0xffe1, true);
		rfb.writeKeyEvent(KeyEvent.VK_L, true);
// Shift Modifier down
		rfb.writeKeyEvent(0xffe1, false);

		rfb.os.write(rfb.eventBuf, 0, rfb.eventBufLen);

// rfb.eventBufLen=0;
// rfb.write( KeyEvent.KEY_RELEASED, true);
// rfb.os.write(rfb.eventBuf, 0, rfb.eventBufLen);

		rfb.eventBufLen = 0;
		rfb.writeKeyEvent(KeyEvent.VK_I, true);
		rfb.os.write(rfb.eventBuf, 0, rfb.eventBufLen);

		rfb.eventBufLen = 0;

		rfb.writeKeyEvent(0xffe1, true);
		rfb.writeKeyEvent(KeyEvent.VK_L, true);
// Shift Modifier down
		rfb.writeKeyEvent(0xffe1, false);
		rfb.os.write(rfb.eventBuf, 0, rfb.eventBufLen);

	}

	void sendClose() throws IOException {
		rfb.os.flush();

		rfb.writeVersionMsg();
		rfb.close();
	}

	void connectAndAuthenticate() throws IOException {

		showConnectionStatus("Connecting to " + host + ", port " + port + "...");

		rfb = new RfbProto(host, port, null);
		showConnectionStatus("Connected to server");

		rfb.readVersionMsg();
		showConnectionStatus("RFB server supports protocol version " + rfb.serverMajor + "." + rfb.serverMinor);

		rfb.writeVersionMsg();
		showConnectionStatus("Using RFB protocol version " + rfb.clientMajor + "." + rfb.clientMinor);

		int secType = rfb.negotiateSecurity();
		int authType;
		if(secType == RfbProto.SecTypeTight) {
			showConnectionStatus("Enabling TightVNC protocol extensions");
			rfb.setupTunneling();
			authType = rfb.negotiateAuthenticationTight();
		} else {
			authType = secType;
		}

		switch(authType) {
			case RfbProto.AuthNone:
				showConnectionStatus("No authentication needed");
				rfb.authenticateNone();
				break;
			case RfbProto.AuthVNC:
				showConnectionStatus("Performing standard VNC authentication");
				if(password != null) {
					try {
						rfb.authenticateVNC(password);
					} catch(Exception ex) {
						System.err.println("Error authenticating " + ex.toString());
						System.exit(-1);
					}
				} else {
					System.err.println("Server requires a password");
					System.exit(-1);
				}
				break;
			default:
				throw new IOException("Unknown authentication scheme " + authType);
		}
	}

	void showConnectionStatus(String msg) {
		System.out.println(msg);
		return;
	}

	static ArrayList<Integer> stringToKeyCodesList(String fullString) {

		String parseString = fullString;

		boolean found = false;
		String match = "";
		ArrayList<Integer> finalSequence = new ArrayList<Integer>();

		while(parseString.length() > 0) {

			Iterator<String> modifiersIterator = VncMappings.MODIFIER_MAP.keySet().iterator();
			while(modifiersIterator.hasNext() && !found) {
				String modifier = modifiersIterator.next();
				if(parseString.startsWith(modifier)) {
					int code = VncMappings.MODIFIER_MAP.get(modifier);
					finalSequence.add(code);
					found = true;
					match = modifier;
				}
			}

			Iterator<String> specialKeysIterator = VncMappings.SPECIAL_KEYMAP.keySet().iterator();
			while(specialKeysIterator.hasNext() && !found) {
				String specialKey = specialKeysIterator.next();
				if(parseString.startsWith(specialKey)) {
					int code = VncMappings.SPECIAL_KEYMAP.get(specialKey);
					int b = code + 0xff00;

					finalSequence.add(b);
					found = true;
					match = specialKey;

				}
			}

			Iterator<String> SequencesIterator = VncMappings.SEQUENCES_MAP.keySet().iterator();
			while(SequencesIterator.hasNext() && !found) {
				String sequence = SequencesIterator.next();
				if(parseString.startsWith(sequence)) {
					Integer keycodes[] = VncMappings.SEQUENCES_MAP.get(sequence);

					for(int i = 0; i < keycodes.length; i++) {
						finalSequence.add(keycodes[i]);
					}
					found = true;
					match = sequence;
				}
			}

			Iterator<String> KeysIterator = VncMappings.KEYMAP.keySet().iterator();
			while(KeysIterator.hasNext() && !found) {
				String key = KeysIterator.next();
				if(parseString.startsWith(key)) {
					int code = VncMappings.KEYMAP.get(key);
					finalSequence.add(code);
					found = true;
					match = key;

				}
			}

			if(found) {
				parseString = parseString.substring(match.length());
				match = "";
				found = false;
			} else {
				// Pop the character.
				parseString = parseString.substring(1);
			}

		}

		return finalSequence;

	}

}
