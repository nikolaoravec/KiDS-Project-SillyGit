package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;

	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			if (clientMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort()
					&& clientMessage.getSenderIp().equals(clientMessage.getReceiverIp())) {

				String messageText = clientMessage.getMessageText();
				String[] serventsInfo = messageText.split(",");

				List<ServentInfo> allNodes = new ArrayList<>();
				for (String info : serventsInfo) {
					String[] ipAndPort = info.split(":");
					// ipAndPort[0] = "localhost";
					allNodes.add(new ServentInfo(ipAndPort[0], Integer.parseInt(ipAndPort[1])));
				}
				AppConfig.chordState.addNodes(allNodes);

				ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
						AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
						AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getNextNodeIp(),
						AppConfig.chordState.getNextNode().getChordId(), "Update succesfull!");

				MessageUtil.sendMessage(releaseMutexMessage);

			} else {
				ServentInfo newNodeInfo = new ServentInfo(clientMessage.getSenderIp(), clientMessage.getSenderPort());
				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodeInfo);

				AppConfig.chordState.addNodes(newNodes);
				String newMessageText = "";

				if (clientMessage.getMessageText().equals("")) {
					newMessageText = String.valueOf(
							AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort());
				} else {
					newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getIpAddress() + ":"
							+ AppConfig.myServentInfo.getListenerPort();
				}
				Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), clientMessage.getSenderIp(),
						AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getNextNodeIp(), newMessageText);

				MessageUtil.sendMessage(nextUpdate);
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

}
