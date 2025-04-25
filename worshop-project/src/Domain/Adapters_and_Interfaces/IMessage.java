package Domain.Adapters_and_Interfaces;



public interface IMessage {
    void send(IMessageListener receiver);
    void respond(IMessage respondTo);
    void setNext(IMessage next);
    void setPrevious(IMessage previous);
    int getId();
}

