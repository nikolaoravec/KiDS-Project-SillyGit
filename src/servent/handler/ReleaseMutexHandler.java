package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.util.MessageUtil;

public class ReleaseMutexHandler implements MessageHandler {

	private Message clientMessage;

	public ReleaseMutexHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		try {
			if (clientMessage.getMessageType() == MessageType.RELEASE_MUTEX) {
				ReleaseMutexMessage releaseMutexMessage = (ReleaseMutexMessage) clientMessage;
				int chordId = Integer.parseInt(releaseMutexMessage.getMessageText());
				if (AppConfig.myServentInfo.getChordId() == chordId) {

					if (!(releaseMutexMessage.getFileName().equals("") && releaseMutexMessage.getNewVersion() == -1)) {
						AppConfig.chordState.getFileVersions().put(releaseMutexMessage.getFileName(),
								releaseMutexMessage.getNewVersion());
					}

					AppConfig.releaseBothMutex();
					System.out.println(releaseMutexMessage.getMessageTextReturn());
					// System.out.println("Izasao sam iz LOCK!!");
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage rmm = new ReleaseMutexMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							chordId, releaseMutexMessage.getMessageTextReturn());

					MessageUtil.sendMessage(rmm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
