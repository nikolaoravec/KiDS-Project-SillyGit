package cli.command;

import java.io.File;

import app.AppConfig;
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
	

		if (!args.equals("")) {
			String fileName = "";

			fileName = args;

			System.out.println(fileName);
			File file = new File(AppConfig.WORK_ROUTE_PATH);
			File fileToCommit = AppConfig.fileConfig.findFile(file, fileName);

			if (fileToCommit != null) {
				String absolutePath1 = fileToCommit.getAbsolutePath();
				String absolutePath2 = file.getAbsolutePath() + "\\";
				String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);
				System.out.println(file.getAbsolutePath());
				System.out.println(relative);
				if (fileToCommit.isDirectory()) {

//						ArrayList<String> children = addDirToValue(relativePath,fileToAdd, true);
//						int hash = ChordState.chordHash(relativePath);
//						
//						AppConfig.chordState.putValue(hash, "", "","",relativePath,true,children);

				} else {
					System.out.println(fileToCommit.getAbsolutePath());
					int hashFileName = AppConfig.chordState.chordHash(relative);
					AppConfig.chordState.commit(hashFileName, fileName,
							AppConfig.fileConfig.getFileContent(fileToCommit),
							AppConfig.fileConfig.getFileExtension(fileToCommit), false, null, AppConfig.chordState.getFileVersions().get(relative));

				}
			} else {
				AppConfig.timestampedErrorPrint("I dont' have that file in my workspace");
				AppConfig.releaseBothMutex();
			}

		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
			AppConfig.releaseBothMutex();
		}

	}

}
