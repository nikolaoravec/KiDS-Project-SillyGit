package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import servent.handler.AddHandler;
import servent.handler.AskPullHandler;
import servent.handler.CommitHandler;
import servent.handler.ConflictHandler;
import servent.handler.GiveMyFilesAcceptedHandler;
import servent.handler.GiveMyFilesHandler;
import servent.handler.MessageHandler;
import servent.handler.NewNodeHandler;
import servent.handler.NullHandler;
import servent.handler.PushHandler;
import servent.handler.QuitMessageHandler;
import servent.handler.ReleaseMutexHandler;
import servent.handler.RemoveMessageHandler;
import servent.handler.SetPredecessorHandler;
import servent.handler.SorryHandler;
import servent.handler.TellPullHandler;
import servent.handler.TokenHandler;
import servent.handler.UpdateHandler;
import servent.handler.WelcomeHandler;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	public SimpleServentListener() {
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
				case NEW_NODE:
					messageHandler = new NewNodeHandler(clientMessage);
					break;
				case WELCOME:
					messageHandler = new WelcomeHandler(clientMessage);
					break;
				case SORRY:
					messageHandler = new SorryHandler(clientMessage);
					break;
				case UPDATE:
					messageHandler = new UpdateHandler(clientMessage);
					break;
				case ADD:
					messageHandler = new AddHandler(clientMessage);
					break;
				case ASK_GET:
					messageHandler = new AskPullHandler(clientMessage);
					break;
				case TELL_GET:
					messageHandler = new TellPullHandler(clientMessage);
					break;
				case TOKEN:
					messageHandler = new TokenHandler(clientMessage);
					break;
				case RELEASE_MUTEX:
					messageHandler = new ReleaseMutexHandler(clientMessage);
					break;
				case COMMIT:
					messageHandler = new CommitHandler(clientMessage);
					break;
				case REMOVE:
					messageHandler = new RemoveMessageHandler(clientMessage);
					break;
				case QUIT:
					messageHandler = new QuitMessageHandler(clientMessage);
					break;
				case GIVEMYFILES:
					messageHandler = new GiveMyFilesHandler(clientMessage);
					break;
				case GIVE_ACCEPTED:
					messageHandler = new GiveMyFilesAcceptedHandler(clientMessage);
					break;
				case SET_PRED:
					messageHandler = new SetPredecessorHandler(clientMessage);
					break;
				case PUSH:
					messageHandler = new PushHandler(clientMessage);
					break;
				case CONFLICT:
					messageHandler = new ConflictHandler(clientMessage);
					break;
				case POISON:
					break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
