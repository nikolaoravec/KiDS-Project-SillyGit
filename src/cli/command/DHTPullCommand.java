package cli.command;


import app.AppConfig;
import app.ChordState;
import mutex.TokenMutex;

public class DHTPullCommand implements CLICommand {

	@Override
	public String commandName() {
		return "pull";
	}

	@Override
	public void execute(String args) {
		
			try {
				AppConfig.mutex.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			TokenMutex.lock();
			System.out.println("usao sam u pull komandu");
			String[] splitArgs = args.split(" ");
			String fileName = "";
			int version = -1;
//			No version
			if (splitArgs.length == 1) {
				fileName = splitArgs[0];
				
//				Version found
			} 
			if (splitArgs.length == 2) {
				fileName = splitArgs[0];
				try {
				version = Integer.parseInt(splitArgs[1]);
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint("Invalid argument for dht_get: " + args + ". Should be version, which is an int.");
				}
				
			}
			int hashFileName = ChordState.chordHash(fileName);
			AppConfig.chordState.getValue(AppConfig.myServentInfo,hashFileName, version);

		
	}

}
