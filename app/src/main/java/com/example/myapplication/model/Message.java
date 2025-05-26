package com.example.myapplication.model;

public class Message {
    private String senderID;
    private String sender;
    private String message;



    public Message() {} // Bắt buộc cho Firebase

    public Message(String senderID, String sender, String message) {
        this.senderID = senderID;
        this.sender = sender;
        this.message = message;
    }
    public String getSenderID(){
        return senderID;
    }
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
