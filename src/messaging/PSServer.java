/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Publish/Subscriber central server
 * @author luisarmando
 */
public class PSServer implements MessagesOrganizer {
    /* Listen for incomming messages */

    public PeerListener listener;
    //public subscribers;
    public Map<String, Subscriber> subscribers;
    public Map<String, Map<String, Queue<Message>>> messages;

    public PSServer() {
        this.subscribers = new HashMap<String, Subscriber>();
        this.messages = new ConcurrentHashMap<String, Map<String, Queue<Message>>>();
        listener = new PeerListener((MessagesOrganizer) this);
        listener.setPort(2001);
        listener.start();
    }

    public Collection<Message> read(String topic, String title) {
        Collection<Message> list = new ArrayList<Message>();
        /*for (Message msg : queue) {
            if (((topic == null)
                    || msg.topic.equals(topic))
                    && ((title == null)
                    || (msg.title.equals(title)))) {
                list.add(msg);
            }
        }*/
        return list;
    }

    public Collection<Message> read(String topic) {
        return read(topic, null);
    }

    public void subscribe(String address, String topic, String title) {
        if (this.subscribers.containsKey(address)) {
            Subscriber subscriber = this.subscribers.get(address);
            subscriber.subscribe(topic, title);
        } else {
            Subscriber subscriber = new Subscriber(address);
            subscriber.subscribe(topic, title);
            this.subscribers.put(address, subscriber);
        }
    }
    public static int defaultPort = 1001;

    public boolean dispatchToSubscribers() {
        for (Subscriber subscriber : this.subscribers.values()) {
            Socket socket = null;
            String[] addport = subscriber.address.split(":");
            String address = addport[0];
            int port;
            if (addport.length < 2) {
                port = this.defaultPort;
            } else {
                port = Integer.parseInt(addport[1]);
            }
            try {
                socket = new Socket(InetAddress.getByName(address), port);
                ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
                //Send object over the network
                for (String topic : subscriber.subscription.keySet()) {
                    if (!this.messages.containsKey(topic)) {
                        continue;
                    }
                    Set<String> titleList = subscriber.subscription.get(topic);

                    for (String title : this.messages.get(topic).keySet()) {
                        boolean found = titleList.isEmpty();
                        for (String titleSubscription : titleList) {
                            if (title.equals(titleSubscription)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            for (Message message : this.messages.get(topic).get(title)) {
                                if (message.timestampReciever.compareTo(subscriber.lastTimestamp) > 0) {
                                    oo.writeObject(message);
                                    oo.flush();
                                    subscriber.lastTimestamp = message.timestampReciever;
                                }
                            }
                        }
                    }
                }
                socket.close();
                return true;
            } catch (IOException ioe) {
                System.out.println(ioe);
                return false;
            }
        }
        return true;
    }

    public Collection<String> getTopics() {
        return this.messages.keySet();
    }

    public Collection<String> getTitles(String topic) {
        if (!this.messages.containsKey(topic)) {
            return new HashSet<String>();
        } else {
            return this.messages.get(topic).keySet();
        }
    }

    @Override
    public boolean addMessage(Message message) {

        if (!this.messages.containsKey(message.topic)) {
            this.messages.put(message.topic, new ConcurrentHashMap<String, Queue<Message>>());
        }
        Map<String, Queue<Message>> topicList = this.messages.get(message.topic);
        if (!topicList.containsKey(message.title)) {
            topicList.put(message.title, new PriorityBlockingQueue<Message>());
        }
        return topicList.get(message.title).add(message);
    }

    public void registerTopicTitles(List<Entry<String, String>> list) {
        for (Entry<String, String> entry : list) {
            if (!this.messages.containsKey(entry.getKey())) {
                this.messages.put(entry.getKey(), new ConcurrentHashMap<String, Queue<Message>>());
            }
            Map<String, Queue<Message>> topicList = this.messages.get(entry.getKey());
            if (!topicList.containsKey(entry.getValue())) {
                topicList.put(entry.getValue(), new PriorityBlockingQueue<Message>());
            }
        }
    }
}