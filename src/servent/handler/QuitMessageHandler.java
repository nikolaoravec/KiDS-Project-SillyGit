package servent.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.SetPredecessorMessage;
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

				SetPredecessorMessage setPredecessorMessage = new SetPredecessorMessage(
						AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
						AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getNextNodeIp(),
						AppConfig.chordState.getPredecessor().getIpAddress(),
						AppConfig.chordState.getPredecessor().getListenerPort());

				MessageUtil.sendMessage(setPredecessorMessage);

				File f = new File(AppConfig.STORAGE_PATH);

				AppConfig.fileConfig.deleteDirectory(f);

				AppConfig.releaseBothMutex();

				// System.out.println("Storage is deleted.");

				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// timeout

				AppConfig.ssl.stop();

				// DO UNLOCK AND WAIT TO CLOSE SSL

			} else {

				QuitMessage nextQuitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNodePort(),
						AppConfig.chordState.getNextNodeIp(), chordId);

				MessageUtil.sendMessage(nextQuitMessage);

				List<ServentInfo> newList = new ArrayList<>();
				for (ServentInfo s : AppConfig.chordState.allNodeInfo) {
					if (s.getChordId() != chordId) {
						newList.add(s);
					}
				}
				AppConfig.chordState.setAllNodeInfo(newList);

				AppConfig.chordState.updateSuccessorTable();

			}
		}
	}
}
