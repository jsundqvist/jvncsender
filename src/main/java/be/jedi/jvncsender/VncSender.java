package be.jedi.jvncsender;

import com.google.common.collect.ImmutableList;
import com.tightvnc.vncviewer.VncSenderConnection;

import java.io.IOException;

public class VncSender {

   int vncWaitMillis = 1;

   String vncHost;
   int vncPort;
   String vncPassword;
   private final VncSenderConnection jvnc;

   public VncSender(String vncHost, int vncPort, String vncPassword) throws Exception {
      super();
      this.vncHost = vncHost;
      this.vncPort = vncPort;
      this.vncPassword = vncPassword;
      jvnc = new VncSenderConnection(vncHost, vncPort, vncPassword);
      jvnc.open();
   }

   public void send(String vncText) throws Exception {
      String[] vncTextArray = new String[1];
      vncTextArray[0] = vncText;
      this.send(vncTextArray);
   }

   public void send(String... vncText) throws Exception {
      send(ImmutableList.copyOf(vncText));
   }

   public void send(Iterable<String> vncText) throws Exception {

      // Ignore Cert file
      // https://www.chemaxon.com/forum/ftopic65.html&highlight=jmsketch+signer

      for (String line : vncText) {
         System.out.println("Sending line: " + line);

         for(char c : line.toCharArray()) {
            jvnc.print("" + c);
            sleep(vncWaitMillis);
         }

      }

   }

   public void sendKey(int key) throws IOException {
      jvnc.writeKeyEvent(key);
   }

   void sleep(int millis) {
      // We need to wait
      try {
         Thread.sleep(millis);// sleep for 1000 ms
      } catch (InterruptedException ie) {
      }
   }

   public int getVncWaitMillis() {
      return vncWaitMillis;
   }

   public void setVncWaitMillis(int vncWaitMillis) {
      this.vncWaitMillis = vncWaitMillis;
   }

   public String getVncHost() {
      return vncHost;
   }

   public void setVncHost(String vncHost) {
      this.vncHost = vncHost;
   }

   public int getVncPort() {
      return vncPort;
   }

   public void setVncPort(int vncPort) {
      this.vncPort = vncPort;
   }

   public String getVncPassword() {
      return vncPassword;
   }

   public void setVncPassword(String vncPassword) {
      this.vncPassword = vncPassword;
   }

}
