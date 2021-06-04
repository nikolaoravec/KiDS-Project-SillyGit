package servent.message;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class GiveMyFilesMessage extends BasicMessage {

	/**
	 * Used to send data of my files to next node
	 */
	private static final long serialVersionUID = -2754306286061208512L;

	private Map<Integer, ArrayList<File>> valueMap;

	public GiveMyFilesMessage(int senderPort, String senderIp, int receiverPort, String receiverIp,
			Map<Integer, ArrayList<File>> valueMap, int chorId) {
		super(MessageType.GIVEMYFILES, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chorId));
		this.valueMap = valueMap;

	}

	public Map<Integer, ArrayList<File>> getValueMap() {
		return valueMap;
	}

}
