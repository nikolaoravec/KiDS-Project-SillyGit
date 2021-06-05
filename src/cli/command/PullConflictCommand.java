package cli.command;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import model.ConflictObject;
import mutex.TokenMutex;

public class PullConflictCommand  implements CLICommand {
	
	@Override
	public String commandName() {
		return "pull_conflict";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u pull conflict komandi");
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];
				
				int hashFileName = ChordState.chordHash(fileName);
				
				ConflictObject conflictObject = AppConfig.myServentInfo.getConflicts().get(hashFileName);
				
				if(conflictObject != null) {
					File conflictFile = new File(AppConfig.WORK_ROUTE_PATH + File.separator + fileName + conflictObject.getExtension());
					AppConfig.fileConfig.setFileContent(conflictFile, conflictObject.getContent());
					AppConfig.chordState.getFileVersions().put(fileName,conflictObject.getVersion());
					AppConfig.myServentInfo.getConflicts().remove(hashFileName);
					AppConfig.timestampedStandardPrint("Pull_conflict command succesfull!");
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