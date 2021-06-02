package cli.command;


import java.io.File;
import java.util.List;

import app.AppConfig;
import app.ChordState;

public class DHTPullCommand implements CLICommand {

	@Override
	public String commandName() {
		return "pull";
	}

	@Override
	public void execute(String args) {
		
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
			System.out.print("hash za folder " + hashFileName);
			List<File> val = AppConfig.chordState.getValue(hashFileName, version);
			
//			if (val == -2) {
//				AppConfig.timestampedStandardPrint("Please wait...");
//			} else if (val == -1) {
//				AppConfig.timestampedStandardPrint("No such key: " + key);
//			} else {
//				AppConfig.timestampedStandardPrint(key + ": " + val);
//			}
		
	}

}
