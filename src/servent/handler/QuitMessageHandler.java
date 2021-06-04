package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

public class QuitMessageHandler implements MessageHandler {

	private Message clientMessage;

	public QuitMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.QUIT) {
			QuitMessage quitMessage = (QuitMessage) clientMessage;
			int chordId = Integer.parseInt(quitMessage.getMessageText());

			if (AppConfig.myServentInfo.getChordId() == chordId) {

				AppConfig.releaseBothMutex();

				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// timeout

				AppConfig.ssl.stop();

//				DO UNLOCK AND WAIT TO CLOSE SSL

			} else {

				QuitMessage nextQuitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNodePort(),
						AppConfig.chordState.getNextNodeIp(), chordId);

				MessageUtil.sendMessage(nextQuitMessage);

				for (ServentInfo s : AppConfig.chordState.allNodeInfo) {
					if (s.getChordId() == chordId) {
						AppConfig.chordState.allNodeInfo.remove(s);
					}
				}
//				try {
//					Thread.sleep(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}

				AppConfig.chordState.updateSuccessorTable();
//				ServentInfo[] newSuccesorTable = new ServentInfo[AppConfig.chordState.getSuccessorTable().length - 1];
//				int index = 0;
//				for (int i = 0; i < AppConfig.chordState.getSuccessorTable().length; i++) {
//					if (!(AppConfig.chordState.getSuccessorTable()[i].getChordId() == chordId)) {
//						newSuccesorTable[index++] = AppConfig.chordState.getSuccessorTable()[i];
//					}
//				}
//				
//				for (int i = 0; i < index; i++) {
//					AppConfig.chordState.getSuccessorTable()[i] = newSuccesorTable[i];
//				}
//				System.out.println("Old: " + AppConfig.chordState.getSuccessorTable().toString());
//				System.out.println("New: " + newSuccesorTable.toString());
//				int len = AppConfig.chordState.getSuccessorTable().length;
//				AppConfig.chordState.getSuccessorTable()[len-1] = null;

			}

		}
	}
}
