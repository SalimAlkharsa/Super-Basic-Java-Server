import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.xml.crypto.Data;

public class Talker {
    private static final int MAX_LENGTH = 50;
    private static final int TIMEOUT = 2000;

    // Requirements for the talker
    // Accept a string of up to 50 characters entered in cmd line OK
    // Break up the string into messages of 10 characters (so 5 messages) OK
    // Each message should be numbered in order OK
    // Message 0 is gonna contain the number of messages sent OK
    // Talker must wait for a listener to request a UDP connection then accept it OK
    // Talker should then request a UDP connection to the listener, to get ACK OK
    // Ports for Talker and Listener should be entered in cmd line OK
    // Talker should extract listener's IP address to request UDP connection OK
    // Talker should send each message to the listener using UDP OK
    // Talker may only send a new message only if it has received an ACK for the
    // previous message
    // Talker should wait for a timeout of 2 seconds for an ACK before resending the
    // message
    public static void main(String[] args) {
        // Check if the user has entered the message and ports for talker and listener
        if (args.length != 3) {
            System.out.println("Usage: java Talker <message> <talker_port> <listener_port>");
            return;
        }

        // Parse args
        String message = args[0];
        int talkerPort = Integer.parseInt(args[1]);
        int listenerPort = Integer.parseInt(args[2]);

        // Check if the message is less than 50 characters
        if (!verifyMessage(message)) {
            return;
        }

        // Break up the message into 10 character messages
        String[] messages = processMessage(message);
        DatagramSocket socket = null;
        boolean Acked = false;

        // Wait for a UDP connection from the listener
        try {
            socket = new DatagramSocket(talkerPort);
            socket.setSoTimeout(TIMEOUT);
            System.out.println("Waiting for a UDP connection from the listener");

            // Create a buffer to store incoming data
            byte[] buffer = new byte[1024];
            // Create a datagram packet to receive the incoming data
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // Wait for the listener to send a UDP packet
            socket.receive(packet);

            // Get the listener's IP address
            String listenerAddress = packet.getAddress().getHostAddress();
            System.out.println("Listener's IP address: " + listenerAddress);

            // Send a UDP packet to the listener to request a connection
            String request = "Requesting UDP connection";
            buffer = request.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(listenerAddress),
                    listenerPort);
            socket.send(packet);

            // Wait for the listener to send a UDP packet
            socket.receive(packet);

            // By now we have made a UDP connection with the listener
            System.out.println("Made a UDP connection with the listener");

            // Send the messages to the listener
            for (int i = 0; i < messages.length; i++) {
                sendMessage(socket, InetAddress.getByName(listenerAddress), listenerPort, i, messages.length,
                        messages[i]);
                Acked = waitForACK(socket, InetAddress.getByName(listenerAddress), listenerPort, i, i + 1);
                if (!Acked) {
                    i--;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // Function to verify the message is less than 50 characters
    private static boolean verifyMessage(String message) {
        if (message.length() > MAX_LENGTH) {
            System.out.println("Message is too long");
            return false;
        }
        return true;
    }

    // Function to break up the message into 10 character messages
    private static String[] processMessage(String message) {
        // Get the number of messages
        int numMessages = (int) Math.ceil(message.length() / 10.0) + 1;
        // Create an array to store the messages
        String[] messages = new String[numMessages];
        // Loop through the message and break it up into 10 character messages
        int start;
        int end;
        // set the initial message to be the number of messages
        messages[0] = Integer.toString(numMessages);
        for (int i = 1; i < numMessages; i++) {
            // Get the start and end index of the message
            start = (i - 1) * 10;
            end = Math.min(start + 10, message.length());
            // Get the message
            messages[i] = message.substring(start, end);
        }
        // Return the messages
        return messages;
    }

    // Function to send a message to the listener
    private static void sendMessage(DatagramSocket socket, InetAddress address, int listenerPort,
            int sequenceNumber, int numMessages, String message) {
        // Make the message frame
        String frame = Integer.toString(sequenceNumber) + "|" + Integer.toString(numMessages) + "|" + message;
        // Create a buffer to store the message
        byte[] buffer = frame.getBytes();
        // Create a datagram packet to send the message
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, listenerPort);
        try {
            socket.send(packet);
            System.out.println("Sent message: " + sequenceNumber + ":" + frame);
        } catch (Exception e) {
            System.out.println("Error sending message: " + e);
        }
    }

    // Function to wait for an ACK from the listener
    private static boolean waitForACK(DatagramSocket socket, InetAddress listenerAddress, int listenerPort,
            int sequenceNumber, int expectedACK) {
        try {
            // Create a buffer to store the incoming data
            byte[] buffer = new byte[1024];
            // Create a datagram packet to receive the incoming data
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            // Get the message from the packet
            String message = new String(packet.getData(), 0, packet.getLength());
            // See what was received
            int ack = Integer.parseInt(message);
            if (ack != expectedACK) {
                System.out.println("Expected ACK: " + expectedACK + " but got: " + ack);
                return false;
            } else {
                System.out.println("Got ACK: " + ack);
                return true;
            }
        } catch (Exception IOexception) {
            System.out.println("Error waiting for ACK: " + IOexception);
        }
        return true;
    }
}
