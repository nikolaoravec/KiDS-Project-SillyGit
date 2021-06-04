package cli.command;

import app.AppConfig;
import app.ChordState;
import mutex.TokenMutex;

public class RemoveCommand  implements CLICommand {
	
	@Override
	public String commandName() {
		return "remove";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u Remove komandi");
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];
				
				int hashFileName = ChordState.chordHash(fileName);
				AppConfig.chordState.remove(hashFileName);
					 
			
			} catch (Exception e) {
				AppConfig.timestampedErrorPrint(e.getMessage());
				AppConfig.releaseBothMutex();
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
			AppConfig.releaseBothMutex();
		}

	}
	

}
