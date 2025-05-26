package com.example.myapplication.model;

public class MessageModel {
    private String senderId;
    private String sender;
    private String text;
    private boolean isSentByMe;

    public MessageModel() {} // Firebase cần constructor rỗng

    public MessageModel(String senderId,String sender, String text, boolean isSentByMe) {
        this.senderId = senderId;
        this.sender = sender;
        this.text = text;
        this.isSentByMe = isSentByMe;
    }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public boolean isSentByMe() { return isSentByMe; }

    public void setSender(String sender) { this.sender = sender; }
    public void setText(String text) { this.text = text; }
    public void setSentByMe(boolean sentByMe) { isSentByMe = sentByMe; }
}
