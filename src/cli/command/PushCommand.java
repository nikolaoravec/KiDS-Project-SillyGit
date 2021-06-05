package cli.command;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import model.ConflictObject;
import mutex.TokenMutex;

public class PushCommand implements CLICommand {

	@Override
	public String commandName() {
		return "push";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u Push komandi");

		if (!args.equals("")) {
			String fileName = "";

			fileName = args;

			// System.out.println(fileName);
			File file = new File(AppConfig.WORK_ROUTE_PATH);
			File fileToCommit = AppConfig.fileConfig.findFile(file, fileName);

			if (fileToCommit != null) {
				String absolutePath1 = fileToCommit.getAbsolutePath();
				String absolutePath2 = file.getAbsolutePath() + "\\";
				String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);
				// System.out.println(file.getAbsolutePath());
				// System.out.println(relative);
				if (fileToCommit.isDirectory()) {

					// ArrayList<String> children = addDirToValue(relativePath,fileToAdd, true);
					// int hash = ChordState.chordHash(relativePath);
					//
					// AppConfig.chordState.putValue(hash, "", "","",relativePath,true,children);

				} else {
					int hashFileName = ChordState.chordHash(relative);
					ConflictObject conflictObject = AppConfig.myServentInfo.getConflicts().get(hashFileName);
					AppConfig.chordState.push(hashFileName, fileName, AppConfig.fileConfig.getFileContent(fileToCommit),
							AppConfig.fileConfig.getFileExtension(fileToCommit), false, null,
							conflictObject.getVersion());

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
