package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.AddMessage;

public class AddHandler implements MessageHandler {

	private Message clientMessage;

	public AddHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ADD) {
			AddMessage addMessage = (AddMessage) clientMessage;

			try {
				int hash = Integer.parseInt(addMessage.getMessageText());

				AppConfig.chordState.putValue(hash, addMessage.getFileName(), addMessage.getContent(), addMessage.getExtension());
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
