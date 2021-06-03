package cli.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
				File fileToAdd = findFile(file, fileName);
				
				if(fileToAdd != null) {
					String absolutePath1 = fileToAdd.getAbsolutePath();
				    String absolutePath2 = file.getAbsolutePath() + "\\";
				    String relativePath = absolutePath1.substring(absolutePath2.length());
				    
					if(fileToAdd.isDirectory()) {
						
						ArrayList<String> children = addDirToValue(relativePath,fileToAdd, true);
						int hash = ChordState.chordHash(relativePath);
						
						AppConfig.chordState.putValue(hash, "", "","",relativePath,true,children);
						
					}else{
					    addFileToValue(relativePath, fileToAdd.getName(), fileToAdd, false);
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
				    String relative= absolutePath1.substring(absolutePath2.length());
			
				    children.add(relative);
					
					if(files[i].isDirectory()) {
						
						toSearch.add(files[i]);
						ArrayList<String> children2 = addDirToValue(relative,files[i], true);
						int hash = ChordState.chordHash(relative);
						
						if (hash < 0 || hash >= ChordState.CHORD_SIZE) {
							throw new NumberFormatException();
						}
						if (hash < 0) {
							throw new NumberFormatException();
						}
						
						AppConfig.chordState.putValue(hash, "", "","",relative,true,children2);
					}else {
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
					relative,
					isDir,
					null);
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
			return ""; 
		}
		return name.substring(lastIndexOf);
	}

	private String getFileWithoutExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return file.getName();
		}
		return name.substring(0, lastIndexOf);
	}

}
