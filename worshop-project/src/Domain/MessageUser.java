package Domain;

import java.util.Date;

import Domain.Adapters_and_Interfaces.IMessage;
import Domain.Adapters_and_Interfaces.IMessageListener;

public class MessageUser implements IMessage {

    private int id;
    private int senderId;
    private String senderName;
    private int receiverId;
    private Date dateTime;
    private String title;
    private String content;
    private IMessage previous;
    private IMessage next;

    
    public MessageUser(int id, int senderId, String senderName, int receiverId, Date dateTime, String content) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.dateTime = dateTime;
        this.content = content;
        this.previous = null;
        this.next = null;
    }
    @Override
    public void setPrevious(IMessage previous) {
        this.previous = previous;
    }

    @Override
    public void setNext(IMessage next) {
        this.next = next;
    }

    @Override
    public void send(IMessageListener receiver) {
        receiver.acceptMessage(this);
    }

    @Override
    public void respond(IMessage respondTo) {
        //will need to send real time notification to the receiver
        respondTo.setNext(this);
        this.setPrevious(respondTo);
    }


   
    

}