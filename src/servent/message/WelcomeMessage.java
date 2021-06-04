package servent.message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, ArrayList<File>> values;
	private Map<Integer, List<Integer>> childrenHashes;
	
	public WelcomeMessage(int senderPort, String senderIp, int receiverPort,String receiverIp, Map<Integer, ArrayList<File>> values, Map<Integer, List<Integer>> childrenHashes) {
		super(MessageType.WELCOME, senderPort, senderIp,  receiverPort, receiverIp);
		
		this.values = values;
		this.childrenHashes = childrenHashes;
	}
	
	
	public Map<Integer, List<Integer>> getChildrenHashes() {
		return childrenHashes;
	}
	
	public Map<Integer, ArrayList<File>> getValues() {
		return values;
	}
}
