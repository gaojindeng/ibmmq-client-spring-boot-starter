package io.github.gaojindeng.ibm.mq.esb.message;

public class Message {
    private byte[] messageId;
    private final byte[] data;
    private String format;
    private int characterSet;
    private int timeWaitInterval = -1;

    public Message(byte[] messageId, byte[] data) {
        this.characterSet = -1;
        this.messageId = messageId;
        this.data = data;
    }

    public Message(byte[] messageId, byte[] data, String format, int characterSet) {
        this(messageId, data);
        this.format = format;
        this.characterSet = characterSet;
    }

    public Message(String messageId, byte[] data) {
        this.characterSet = -1;
        this.messageId = messageId.getBytes();
        this.data = data;
    }

    public Message(String messageId, byte[] data, String format, int characterSet) {
        this(messageId, data);
        this.format = format;
        this.characterSet = characterSet;
    }

    public byte[] getMessageId() {
        return messageId;
    }

    public byte[] getData() {
        return data;
    }

    public String getFormat() {
        return format;
    }

    public int getCharacterSet() {
        return characterSet;
    }

    public int getTimeWaitInterval() {
        return timeWaitInterval;
    }
}