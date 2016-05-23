package org.vchat.Model;

/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class Chat {
    private int senderId;
    private int receiverId;
    private String message;
    private String time;

    public Chat(int senderId, int receiverId, String message, String time) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.time = time;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
