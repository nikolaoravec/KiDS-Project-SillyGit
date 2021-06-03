package servent.handler;

import app.AppConfig;
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
				if (releaseMutexMessage.getReceiverIp().equals(AppConfig.myServentInfo.getIpAddress())
						&& releaseMutexMessage.getReceiverPort() == AppConfig.myServentInfo.getListenerPort()) {
					TokenMutex.unlock();
					AppConfig.mutex.release();
					System.out.println("Izasao sam iz LOCK!!");
				} else {
					ReleaseMutexMessage rmm = new ReleaseMutexMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNodePort(),
							AppConfig.chordState.getNextNodeIp());

					MessageUtil.sendMessage(rmm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
