package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import mutex.TokenMutex;
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
					TokenMutex.unlock();
					AppConfig.mutex.release();
					System.out.println("Izasao sam iz LOCK!!");
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage rmm = new ReleaseMutexMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							chordId);

					MessageUtil.sendMessage(rmm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
