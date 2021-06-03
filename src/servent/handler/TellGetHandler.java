package servent.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import app.AppConfig;
import mutex.TokenMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class TellGetHandler implements MessageHandler {

	private Message clientMessage;

	public TellGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		try {

			if (clientMessage.getMessageType() == MessageType.TELL_GET) {

				TellGetMessage tellGetMessage = (TellGetMessage) clientMessage;
				if ((AppConfig.myServentInfo.getIpAddress() == tellGetMessage.getReceiverIp())
						&& (AppConfig.myServentInfo.getListenerPort() == tellGetMessage.getReceiverPort())) {
					//File file = tellGetMessage.getFile();

					System.out.println(tellGetMessage.getContent());
					System.out.println(tellGetMessage.getFileName());
					//System.out.println(tellGetMessage.getContent());

					String storage = AppConfig.WORK_ROUTE_PATH + File.separator;
					File newFile = new File(storage + tellGetMessage.getFileName());

					try {
						newFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					try (BufferedWriter bw = new BufferedWriter(new FileWriter(newFile.getAbsolutePath()))) {
						bw.write(tellGetMessage.getContent());
						bw.newLine();
						bw.close();
					} catch (FileNotFoundException e) {
						System.out.println("Error : " + e.getMessage());
					} catch (IOException e) {
						System.out.println("Error : " + e.getMessage());
					}

					TokenMutex.unlock();
					AppConfig.mutex.release();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
