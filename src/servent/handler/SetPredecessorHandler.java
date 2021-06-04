package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SetPredecessorMessage;

public class SetPredecessorHandler implements MessageHandler {

	private Message clientMessage;

	public SetPredecessorHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		if (clientMessage.getMessageType() == MessageType.SET_PRED) {
			
			SetPredecessorMessage setPredecessorMessage = (SetPredecessorMessage) clientMessage;
			ServentInfo predcessor = new ServentInfo(setPredecessorMessage.getIp(), setPredecessorMessage.getPort());
			AppConfig.chordState.setPredecessor(predcessor);
			
		}
	}
}
