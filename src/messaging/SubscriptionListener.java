/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author luisarmando
 */
public class SubscriptionListener extends Thread {
    

    private int port = 1001; /* port the server listens on */

    public SubscribesOrganizer organizer;

    public SubscriptionListener(SubscribesOrganizer organizer) {
        this.organizer = organizer;
    }
    private ServerSocket server = null;

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    public synchronized boolean getIsAlive() {
        return (server != null && server.isBound());
    }
    
    @Override
    public void run() {

        try {
            server = new ServerSocket(getPort()); /* start listening on the port */
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + getPort());
            System.err.println(e);
            System.exit(1);
        }
        System.out.println("Serving at hostname " + server.getInetAddress().getHostName() + " at port " + getPort());


        while (true) {
            Socket client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.err.println(e);
                System.exit(1);
            }


            try {
                /* obtain an input stream to the client *//*
                BufferedReader in = new BufferedReader(new InputStreamReader(
                client.getInputStream()));*/
                ObjectInputStream oi =
                        new ObjectInputStream(client.getInputStream());
                while (client.isConnected()) {
                    //Read serialized object
                    Message message = (Message) oi.readObject();
                    message.stampRecieve(client.getInetAddress().getHostName());
                    organizer.subscribe(client.getInetAddress().getHostName(), ".............");

                    System.out.println(
                            "Server ("
                            + (server.getInetAddress().getHostName() + ":" + server.getLocalPort())
                            + ") got a subscription: " + message);
                    /*DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
                    outToClient.writeBytes("ok hehe");*/
                }

            } catch (ClassNotFoundException cnfe) {
                System.out.println(cnfe);
            } catch (java.io.EOFException bla) {
                ;
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }
}