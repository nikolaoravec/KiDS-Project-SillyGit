package cli.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import app.AppConfig;
import app.ChordState;

public class DHTAddCommand implements CLICommand {

	@Override
	public String commandName() {
		return "add";
	}

	@Override
	public void execute(String args) {
		String[] splitArgs = args.split(" ");

		if (splitArgs.length == 1) {
			String fileName = "";
			try {
				fileName = splitArgs[0];

				File file = new File(AppConfig.WORK_ROUTE_PATH + "/" + fileName);

				if (file.isDirectory()) {
					Queue<File> toSearch = new LinkedList<>();

					toSearch.add(file);
					while (!toSearch.isEmpty()) {
						File current = toSearch.poll();
						if (current.isDirectory()) {
							File[] files = current.listFiles();

							for (int i = 0; i < files.length; i++) {
								if (files[i].isDirectory()) {
									toSearch.add(files[i]);
								} else {
									addFileToValue(fileName, files[i].getName(), files[i]);
								}
							}
						}
					}
				} else {
					addFileToValue(fileName, file.getName(), file);
				}

			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= "
						+ ChordState.CHORD_SIZE + ". 0 <= value.");
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
		}

	}

	private void addFileToValue(String name, String fileName, File file) {
		int hashFileName = ChordState.chordHash(name);

		if (hashFileName < 0 || hashFileName >= ChordState.CHORD_SIZE) {
			throw new NumberFormatException();
		}
		if (hashFileName < 0) {
			throw new NumberFormatException();
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			reader.close();
			AppConfig.chordState.putValue(hashFileName, getFileWithoutExtension(file), stringBuilder.toString(),
					getFileExtension(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf);
	}

	private String getFileWithoutExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return file.getName(); // empty extension
		}
		return name.substring(0, lastIndexOf);
	}

}
