package mutex;

import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import app.ChordState;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;


public final class TokenMutex{

	public static volatile AtomicBoolean haveToken = new AtomicBoolean(false);
	public static volatile AtomicBoolean wantLock =  new AtomicBoolean(false);
	
	public static void lock() {
		wantLock.set(true);;
		
		while (!haveToken.get()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void unlock() {
		haveToken.set(false);;
		wantLock.set(false);;
		sendTokenForward();

	}
	
	public static void receiveToken() {
		if (wantLock.get()) {
			haveToken.set(true);
		} else {
			sendTokenForward();
		}
	}
	

	public static void sendTokenForward() {
		if( (Integer)AppConfig.chordState.getNextNodePort() != null) {
			MessageUtil.sendMessage(new TokenMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), AppConfig.chordState.getNextNodePort(),AppConfig.chordState.getNextNodeIp()));
		}else {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			receiveToken();
		}
	}

}
