package servent.message;

public class SetPredecessorMessage extends BasicMessage {

	
	private static final long serialVersionUID = -2754306286061208512L;
	private String ip;
	private int port;
	
	public SetPredecessorMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, 
			String ip, int port) {
		super(MessageType.SET_PRED, senderPort, senderIp, receiverPort, receiverIp, "");
		this.ip =ip;
		this.port = port;
	}
	
	
	public int getPort() {
		return port;
	}
	
	public String getIp() {
		return ip;
	}
	

}
