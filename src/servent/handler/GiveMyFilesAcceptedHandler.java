package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.GiveMyFilesAcceptedMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

public class GiveMyFilesAcceptedHandler implements MessageHandler {

	private Message clientMessage;

	public GiveMyFilesAcceptedHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		if (clientMessage.getMessageType() == MessageType.GIVE_ACCEPTED) {
			GiveMyFilesAcceptedMessage giveMyFilesAcceptedMessage = (GiveMyFilesAcceptedMessage) clientMessage;
			int chordId = Integer.parseInt(giveMyFilesAcceptedMessage.getMessageText());
			if (chordId == AppConfig.myServentInfo.getChordId()) {
				
				QuitMessage quitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNode().getListenerPort(),
						AppConfig.chordState.getNextNode().getIpAddress(), AppConfig.myServentInfo.getChordId());
				MessageUtil.sendMessage(quitMessage);
			} else {
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
				GiveMyFilesAcceptedMessage gmfa = new GiveMyFilesAcceptedMessage(
						AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
						nextNode.getListenerPort(), nextNode.getIpAddress(), chordId);
				MessageUtil.sendMessage(gmfa);
			}
		}
	}
}
