package cli.command;

import app.AppConfig;
import app.ChordState;
import model.ConflictObject;
import mutex.TokenMutex;

public class ViewCommand implements CLICommand {
	
	@Override
	public String commandName() {
		return "view";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u View komandi");
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];
				
				int hashFileName = ChordState.chordHash(fileName);
				
				ConflictObject conflictObject = AppConfig.myServentInfo.getConflicts().get(hashFileName);
				
				if(conflictObject != null) {
					System.out.println("Content of conflicted file: ");
					System.out.println(conflictObject.getContent());
					System.out.println("Choose an option: view [file], pull_conflict [file], push [file]");
					
					AppConfig.releaseBothMutex();
				}else {
					AppConfig.timestampedErrorPrint("There is no conflict file with that name");
					AppConfig.releaseBothMutex();
				}
					 
			
			} catch (Exception e) {
				AppConfig.timestampedErrorPrint(e.getMessage());
				AppConfig.releaseBothMutex();
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for view");
			AppConfig.releaseBothMutex();
		}

	}
	

}

