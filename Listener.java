import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
        if (args.length != 2) {
            System.out.println("Usage: java Listener <talker_port> <listener_port>");
            return;
        }

        // Parse args
        int talkerPort = Integer.parseInt(args[0]);
        int listenerPort = Integer.parseInt(args[1]);

        try {
            // Create a DatagramSocket for the listener
            DatagramSocket socket = new DatagramSocket(listenerPort);

            // Request a UDP connection from the talker
            byte[] requestBuffer = "Requesting Connection".getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length,
                    InetAddress.getLocalHost(), talkerPort);
            socket.send(requestPacket);

            // Accept the UDP connection from the talker
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Extract the number of messages from message 0
            int numberOfMessages = Integer.parseInt(new String(receivePacket.getData()).trim());

            // Listen to the talker and send ACK for each message received
            StringBuilder concatenatedMessages = new StringBuilder();
            for (int i = 1; i <= numberOfMessages; i++) {
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Send ACK for the received message
                String ackMessage = "ACK " + i;
                byte[] ackBuffer = ackMessage.getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, receivePacket.getAddress(),
                        receivePacket.getPort());
                socket.send(ackPacket);
                System.out.println("Received Message " + i + ": " + receivedMessage);

                // Concatenate messages
                concatenatedMessages.append(receivedMessage);
            }

            // Close the connection after receiving all messages
            socket.close();

            // Concatenate the messages to a string
            System.out.println("Concatenated Messages: " + concatenatedMessages.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
