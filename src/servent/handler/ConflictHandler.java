package servent.handler;

import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import model.ConflictObject;
import servent.message.ConflictMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ConflictHandler implements MessageHandler {

	private Message clientMessage;

	public ConflictHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.CONFLICT) {
			ConflictMessage conflictMsg = (ConflictMessage) clientMessage;
			int chordId = Integer.parseInt(conflictMsg.getMessageText());
			int hash = conflictMsg.getHash();

			if (AppConfig.myServentInfo.getChordId() == chordId) {

				if (conflictMsg.isConflict()) {

					ConflictObject conflictObject = new ConflictObject(conflictMsg.getVersion(),
							conflictMsg.getContent(), conflictMsg.getFileName(), conflictMsg.getExtension());
					AppConfig.myServentInfo.getConflicts().put(hash, conflictObject);

					System.out.println(AppConfig.myServentInfo.getConflicts().toString());
					System.out.println("You have a conflict for files: ");
					for (Entry<Integer, ConflictObject> valueEntry : AppConfig.myServentInfo.getConflicts()
							.entrySet()) {
						System.out.println(
								AppConfig.fileConfig.getFileNameWithoutVersion(valueEntry.getValue().getFileName()));
					}

					System.out.println("Choose an option: view [file], pull_conflict [file], push [file]");

					AppConfig.releaseBothMutex();

				} else {
					System.out.println("File is commited.");
					AppConfig.releaseBothMutex();
				}

			} else {
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
				ConflictMessage conflictMessage = new ConflictMessage(AppConfig.myServentInfo.getListenerPort(),
						AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
						chordId, hash, conflictMsg.getContent(), conflictMsg.getExtension(), conflictMsg.getFileName(),
						conflictMsg.getVersion(), conflictMsg.isConflict());

				MessageUtil.sendMessage(conflictMessage);
			}

		} else {
			AppConfig.timestampedErrorPrint("Conflict handler got a message that is not CONFLICT");
		}

	}

}
