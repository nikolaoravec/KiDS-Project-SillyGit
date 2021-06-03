package servent.handler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import mutex.TokenMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			int newNodePort = clientMessage.getSenderPort();
			String newNodeIp = clientMessage.getSenderIp();
			ServentInfo newNodeInfo = new ServentInfo(newNodeIp, newNodePort);

			// check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), clientMessage.getSenderPort(),
						clientMessage.getSenderIp());
				MessageUtil.sendMessage(sry);
				return;
			}

			
			// check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { // if yes, prepare and send welcome message

//				System.out.println("Pre locka");
				TokenMutex.lock();
				try {
					AppConfig.mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(!AppConfig.chordState.isKeyMine(newNodeInfo.getChordId())) {
					TokenMutex.unlock();
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
					NewNodeMessage nnm = new NewNodeMessage(newNodePort, newNodeIp, nextNode.getListenerPort(),
							nextNode.getIpAddress());
					MessageUtil.sendMessage(nnm);
				}
				
				System.out.println("Uzeo sam lock u New node handleru");
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}

				AppConfig.chordState.setPredecessor(newNodeInfo);

				Map<Integer, File> myValues = AppConfig.chordState.getValueMap();
				Map<Integer, File> hisValues = new HashMap<>();
				Map<Integer, List<Integer>> myChildrenValues =  AppConfig.chordState.getChildrenHashes();
				Map<Integer, List<Integer>> hisChildrenValues = new HashMap<>();
				

				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();

				for (Entry<Integer, File> valueEntry : myValues.entrySet()) {
					if (hisPredId == myId) { // i am first and he is second
						if (myId < newNodeId) {
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
								hisChildrenValues.put(valueEntry.getKey(), myChildrenValues.get(valueEntry.getKey()));
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
								hisChildrenValues.put(valueEntry.getKey(), myChildrenValues.get(valueEntry.getKey()));
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { // my old predecesor was before me
						if (valueEntry.getKey() <= newNodeId) {
							hisChildrenValues.put(valueEntry.getKey(), myChildrenValues.get(valueEntry.getKey()));
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { // my old predecesor was after me
						if (hisPredId > newNodeId) { // new node overflow
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
								hisChildrenValues.put(valueEntry.getKey(), myChildrenValues.get(valueEntry.getKey()));
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { // no new node overflow
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
								hisChildrenValues.put(valueEntry.getKey(), myChildrenValues.get(valueEntry.getKey()));
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}

					}

				}
				for (Integer key : hisValues.keySet()) { // remove his values from my map
					myValues.remove(key);
				}
				
				for (Integer key : hisChildrenValues.keySet()) { // remove his children values from my map
					myChildrenValues.remove(key);
				}
				AppConfig.chordState.setValueMap(myValues);
				AppConfig.chordState.setChildrenHashes(myChildrenValues);

				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), newNodePort, newNodeIp, hisValues, hisChildrenValues);
				MessageUtil.sendMessage(wm);
			} else { // if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodePort, newNodeIp, nextNode.getListenerPort(),
						nextNode.getIpAddress());
				MessageUtil.sendMessage(nnm);
			}

		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
