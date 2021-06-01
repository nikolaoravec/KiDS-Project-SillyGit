package servent.handler;

import java.io.File;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class AskGetHandler implements MessageHandler {

	private Message clientMessage;

	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {
			try {
				String[]  messageText = clientMessage.getMessageText().split(",");
				
				int hash = Integer.parseInt(messageText[0]);
				int version = Integer.parseInt(messageText[1]);
				
				if (AppConfig.chordState.isKeyMine(hash)) {
					Map<Integer, File> valueMap = AppConfig.chordState.getValueMap(); 
					int value = -1;
					
					if (valueMap.containsKey(hash)) {
						// moramo naci odgovarajucu verziju
						//value = valueMap.get(hash);
					}
					// vraticemo vrvt neki nas objekat koji ce opisivati fajl ili folder
//					TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(),
//															key, value);
					//MessageUtil.sendMessage(tgm);
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
				//	AskGetMessage agm = new AskGetMessage(clientMessage.getSenderPort(), nextNode.getListenerPort(), clientMessage.getMessageText());
				//	MessageUtil.sendMessage(agm);
				}
				
				
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}