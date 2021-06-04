package cli.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import app.AppConfig;
import cli.CLIParser;
import mutex.TokenMutex;
import servent.message.GiveMyFilesMessage;
import servent.message.util.MessageUtil;

public class QuitCommand implements CLICommand {

	private CLIParser parser;

	public QuitCommand(CLIParser parser) {
		this.parser = parser;
	}

	@Override
	public String commandName() {
		return "quit";
	}

	@Override
	public void execute(String args) {

		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TokenMutex.lock();
		
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		String bsIp = AppConfig.BOOTSTRAP_IP;

		try {
			Socket bsSocket = new Socket(bsIp, bsPort);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			String toSend = String.valueOf(AppConfig.myServentInfo.getListenerPort()) + ","
					+ AppConfig.myServentInfo.getIpAddress();
			bsWriter.write("Quit\n" + toSend + "\n");
			bsWriter.flush();

			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		parser.stop();

		GiveMyFilesMessage giveMyFilesMessage = new GiveMyFilesMessage(AppConfig.myServentInfo.getListenerPort(),
				AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNode().getListenerPort(),
				AppConfig.chordState.getNextNode().getIpAddress(), AppConfig.chordState.getValueMap(),
				AppConfig.myServentInfo.getChordId());
		
		MessageUtil.sendMessage(giveMyFilesMessage);

//		Moras obavestiti sve cvorove za izlazak iz sistema
//		CLIParser se gasi odmah, a SSL se gasi tek nakon sto poruka updateChordTable dodje do tebe, plus mora da se saceka
//		da se token preda dalje, jer ako ugasimo pre vremena poruke koje se jos uvek ubradjuju mogu da imajo info da 
//		ti postojis, iako vise nisi tu. 
//		Posaljem 2 poruke, jedna za update succesorTabele svih u sistemu, druga za predavanje mojih prethodnika mom sledbeniku

//		Send giveMyPredMessage -> Send updateSuccTableMessage(QuitMessage) -> turn off CLIParser ->
//		wait till ChordID == AppConfig.myServentInfo.getChordId() -> give Token away, wait a bit than turn off SSL 

	}

}
