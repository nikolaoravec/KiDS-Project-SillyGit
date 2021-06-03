package cli.command;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import app.AppConfig;
import app.ChordState;
import mutex.TokenMutex;

public class DHTAddCommand implements CLICommand {

	@Override
	public String commandName() {
		return "add";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.mutex.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TokenMutex.lock();
		System.out.println("Uzeo sam lock u Add komandi");
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];

				File file = new File(AppConfig.WORK_ROUTE_PATH);
				File fileToAdd = AppConfig.fileConfig.findFile(file, fileName);

				if (fileToAdd != null) {
					String absolutePath1 = fileToAdd.getAbsolutePath();
					String absolutePath2 = file.getAbsolutePath() + "\\";
					String relativePath = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

					if (fileToAdd.isDirectory()) {

						ArrayList<String> children = addDirToValue(relativePath, fileToAdd, true);
						int hash = ChordState.chordHash(relativePath);

						AppConfig.chordState.putValue(hash, "", "", "", relativePath, true, children);

					} else {
						addFileToValue(relativePath, fileToAdd.getName(), fileToAdd, false);
					}
				} else {
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

	private ArrayList<String> addDirToValue(String relativePath, File file, boolean isDir) {

		Queue<File> toSearch = new LinkedList<>();
		ArrayList<String> children = new ArrayList<>();

		toSearch.add(file);
		while (!toSearch.isEmpty()) {
			File current = toSearch.poll();
			if (current.isDirectory()) {
				File[] files = current.listFiles();

				for (int i = 0; i < files.length; i++) {

					String absolutePath1 = files[i].getAbsolutePath();
					String absolutePath2 = new File(AppConfig.WORK_ROUTE_PATH).getAbsolutePath() + "\\";
					String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);
					
					children.add(relative);

					if (files[i].isDirectory()) {

						toSearch.add(files[i]);
						ArrayList<String> children2 = addDirToValue(relative, files[i], true);
						int hash = ChordState.chordHash(relative);

						if (hash < 0 || hash >= ChordState.CHORD_SIZE) {
							throw new NumberFormatException();
						}
						if (hash < 0) {
							throw new NumberFormatException();
						}

						AppConfig.chordState.putValue(hash, "", "", "", relative, true, children2);
					} else {
						addFileToValue(relative, files[i].getName(), files[i], false);
					}

				}
			}
		}
		return children;
	}

	private void addFileToValue(String relative, String fileName, File file, boolean isDir) {
		int hashFileName = ChordState.chordHash(relative);

		if (hashFileName < 0 || hashFileName >= ChordState.CHORD_SIZE) {
			throw new NumberFormatException();
		}
		if (hashFileName < 0) {
			throw new NumberFormatException();
		}

		String content = AppConfig.fileConfig.getFileContent(file);

		AppConfig.chordState.putValue(hashFileName,  AppConfig.fileConfig.getFileWithoutExtension(file), content,  AppConfig.fileConfig.getFileExtension(file),
				relative, isDir, null);

	}


}
