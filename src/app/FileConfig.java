package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class FileConfig {
	
	public String getFileWithoutExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return file.getName();
		}
		return name.substring(0, lastIndexOf);
	}
	
	public String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; 
		}
		return name.substring(lastIndexOf);
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

	public String getFileContent(File file) {
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
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void setFileContent(File newFile, String content) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(newFile.getAbsolutePath()))) {
			bw.write(content);
			bw.newLine();
			bw.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error : " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Error : " + e.getMessage());
		}

	}

	public String getFileNameWithoutVersion(String name) {

		int lastIndexOfUderscore = name.lastIndexOf("_");
		if (lastIndexOfUderscore == -1) {
			return name.substring(0, name.length());
		}
		return name.substring(0, lastIndexOfUderscore);
	}

	public Integer getFileVersion(File file) {
		String name = file.getAbsolutePath();
		int lastIndexOf = name.lastIndexOf(".");
		int lastIndexOfUderscore = name.lastIndexOf("_");
		if (lastIndexOf == -1) {
			return Integer.parseInt(name.substring(lastIndexOfUderscore + 1, name.length()));

		}
		if (lastIndexOfUderscore == -1) {
			return -1;
		}
		return Integer.parseInt(name.substring(lastIndexOfUderscore + 1, lastIndexOf));
	}
	
	public void deleteDirectory(File dir) {
		for (int i = 0; i < dir.listFiles().length; i++) {
			boolean ret = dir.listFiles()[i].delete();
		}
		boolean ret = dir.delete();
	}

	public String getRelativePath(String path1, String path2) {
		if (path2.length() < path1.length()) {
			return path1.substring(path2.length());
		}
		return "";
	}

}
