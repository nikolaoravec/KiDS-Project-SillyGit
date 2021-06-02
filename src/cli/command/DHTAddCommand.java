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

				File file = new File(AppConfig.WORK_ROUTE_PATH);
				File fileToAdd = findFile(file, fileName);
				
				if(fileToAdd != null) {
					if(fileToAdd.isDirectory()) {
						
					//	addDirToValue(fileName, fileName, fileToAdd, fileName, true);
						Queue<File> toSearch = new LinkedList<>();

						toSearch.add(fileToAdd);
						
						while (!toSearch.isEmpty()) {
							File current = toSearch.poll();
							if (current.isDirectory()) {
								File[] files = current.listFiles();
								for (int i = 0; i < files.length; i++) {
									
									if(files[i].isDirectory()) {
										//addDirToValue(files[i].getName(), files[i].getName(), files[i], files[i].getName(), true);
										toSearch.add(files[i]);
									}else{
										String absolutePath1 = files[i].getAbsolutePath();
									    String absolutePath2 = file.getAbsolutePath() + "\\";
									    String relativePath = absolutePath1.substring(absolutePath2.length());
									
									    addFileToValue(getFileExtension(files[i]), files[i].getName(), files[i], relativePath, false);
									}
								}
							}
						}
						
					}else{
					    String absolutePath1 = fileToAdd.getAbsolutePath();
					    String absolutePath2 = file.getAbsolutePath() + "\\";
					    String relativePath = absolutePath1.substring(absolutePath2.length());
					  
					    addFileToValue(fileName, fileToAdd.getName(), fileToAdd, relativePath, false);
					}
				}else {
					AppConfig.timestampedErrorPrint("I dont' have that file in my workspace");
				}

			
				
				

			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= "
						+ ChordState.CHORD_SIZE + ". 0 <= value.");
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
		}

	}
	
	
	public File findFile(File file, String fileName) {
		Queue<File> toSearch = new LinkedList<>();

		toSearch.add(file);
		while (!toSearch.isEmpty()) {
			File current = toSearch.poll();
			if (current.isDirectory()) {
				File[] files = current.listFiles();

				for (int i = 0; i < files.length; i++) {
					
					if(files[i].getName().equals(fileName)) {
						return files[i];
					}
					
					if(files[i].isDirectory()) {
						toSearch.add(files[i]);
					}
							
				}
			}
		}
		return null;
	}
	
	
	
	private void addDirToValue(String name, String fileName, File file, String relativePath, boolean isDir) {
		int hashFileName = ChordState.chordHash(name);
		System.out.println("name of dir " + name + " hash " + hashFileName);
		if (hashFileName < 0 || hashFileName >= ChordState.CHORD_SIZE) {
			throw new NumberFormatException();
		}
		if (hashFileName < 0) {
			throw new NumberFormatException();
		}
		AppConfig.chordState.putValue(hashFileName, "", "","","",true);
		
	}

	private void addFileToValue(String name, String fileName, File file, String relativePath, boolean isDir) {
		int hashFileName = ChordState.chordHash(name);
		System.out.println("name of file " + name + " hash " + hashFileName);
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
			AppConfig.chordState.putValue(hashFileName, 
					getFileWithoutExtension(file), 
					stringBuilder.toString(),
					getFileExtension(file),
					relativePath,
					isDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
