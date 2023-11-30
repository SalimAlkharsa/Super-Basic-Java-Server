import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class Listener {
    // Requirements for the listener
    // Must request a UDP connection from the talker
    // Must accept the UDP connection from the talker
    // Listner should listen to talker right after requesting a UDP connection from
    // the talker
    // Listener and talker ports should be entered in cmd line
    // Listener should extract number of messages from message 0
    // Listener must send an ACK for each message received w ACK number of message
    // number
    // Print what the listener receives
    // Close the connection after receiving all messages and concatenate them to a
    // string
    public static void main(String[] args) {
        // Check if the user has entered the ports for talker and listener
        if (args.length != 3) {
            System.out.println("Usage: java Listener <talker_port> <listener_port> <talker_address>");
            return;
        }

        // Parse args
        int talkerPort = Integer.parseInt(args[0]);
        int listenerPort = Integer.parseInt(args[1]);
        String talkerAddress = args[2];

        DatagramSocket listenSocket = null;
        StringBuilder concatenatedMessages = new StringBuilder();
        try {
            // Listener should request a UDP connection from the talker by sending a message
            listenSocket = new DatagramSocket(listenerPort);

            // Send something to the talker for now
            byte[] requestData = "Howdy Talker!".getBytes();

            // Test FIX ME IP IS LOCALHOST
            InetAddress listenerAddress = InetAddress.getByName(talkerAddress);
            // Backup code if it breaks
            // InetAddress listenerAddress = InetAddress.getLocalHost()
            // FIX ME ^^
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length,
                    listenerAddress, talkerPort);
            listenSocket.send(packet);
            System.out.println("SENT SOMETHING");

            // Listner should listen for a talker
            byte[] recieveData = new byte[1024];
            DatagramPacket recievePacket = new DatagramPacket(recieveData, recieveData.length);
            listenSocket.receive(recievePacket);
            String messageRecieved = new String(recievePacket.getData(), recievePacket.getOffset(),
                    recievePacket.getLength());
            System.out.println("Recieved the following from talker: " + messageRecieved);
            requestData = ("ACK " + 1).getBytes();
            packet = new DatagramPacket(requestData, requestData.length,
                    listenerAddress, talkerPort);
            listenSocket.send(packet);

            // Now recieve the num of messages
            listenSocket.receive(recievePacket);
            messageRecieved = new String(recievePacket.getData(), recievePacket.getOffset(),
                    recievePacket.getLength());
            int expectedMessages = Integer.parseInt(messageRecieved.split("\\|")[1]);
            System.out.println("Expecting this many messages: " + expectedMessages);

            // Now listen as many times as necessary
            String processedMessage;
            Random random = new Random();
            for (int i = 1; i <= expectedMessages; i++) {
                listenSocket.receive(recievePacket);
                // Now process the message
                messageRecieved = new String(recievePacket.getData(), recievePacket.getOffset(),
                        recievePacket.getLength());
                processedMessage = messageRecieved.split("\\|")[2];
                System.out.println("Recieved: " + processedMessage);
                // Above is working
                // Now send an ack
                requestData = ("ACK " + (i + 1)).getBytes();
                packet = new DatagramPacket(requestData, requestData.length,
                        listenerAddress, talkerPort);
                // Now randomly decide to ack or not either generate a 1 or 0
                int randomDecision = random.nextInt(2);
                if (randomDecision == 1) {
                    listenSocket.send(packet);
                    // Concatenate the message
                    concatenatedMessages.append(processedMessage);
                } else {
                    i--;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close listenSocket if not closed
            if (listenSocket != null && !listenSocket.isClosed()) {
                listenSocket.close();
            }
        }
        System.out.println("We have finally finished with a final string of: ");
        System.out.println(concatenatedMessages);
    }
}
