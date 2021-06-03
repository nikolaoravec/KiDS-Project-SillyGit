package cli.command;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import mutex.TokenMutex;

public class CommitCommand implements CLICommand {
	
	@Override
	public String commandName() {
		return "commit";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u Commit komandi");
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];
				

				File file = new File(AppConfig.WORK_ROUTE_PATH);
				File fileToCommit = AppConfig.fileConfig.findFile(file, fileName);
				
				if(fileToCommit != null) {
					String absolutePath1 = fileToCommit.getAbsolutePath();
				    String absolutePath2 = file.getAbsolutePath() + "\\";
				    String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);
				    
					if(fileToCommit.isDirectory()) {
						
//						ArrayList<String> children = addDirToValue(relativePath,fileToAdd, true);
//						int hash = ChordState.chordHash(relativePath);
//						
//						AppConfig.chordState.putValue(hash, "", "","",relativePath,true,children);
						
					}else{
						int hashFileName = AppConfig.chordState.chordHash(relative);
						AppConfig.chordState.commit(hashFileName, fileName, AppConfig.fileConfig.getFileContent(fileToCommit),
								 AppConfig.fileConfig.getFileExtension(fileToCommit), false, null,AppConfig.fileConfig.getFileVersion(fileToCommit));
					 
					}
				}else {
					AppConfig.timestampedErrorPrint("I dont' have that file in my workspace");
					AppConfig.releaseBothMutex();
				}

			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= "
						+ ChordState.CHORD_SIZE + ". 0 <= value.");
				AppConfig.releaseBothMutex();
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
			AppConfig.releaseBothMutex();
		}

	}
	

}
