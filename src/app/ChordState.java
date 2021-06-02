package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import servent.message.AddMessage;
import servent.message.AskGetMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function. It has a
 * static method <code>chordHash</code> which will calculate our chord ids. It
 * also has a static attribute <code>CHORD_SIZE</code> that tells us what the
 * maximum key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 * <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of
 * <code>successorTable</code></li>
 * <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 * <li><code>predecessorInfo</code> - who is our predecessor.</li>
 * <li><code>valueMap</code> - DHT values stored on this node.</li>
 * <li><code>init()</code> - should be invoked when we get the WELCOME
 * message.</li>
 * <li><code>isCollision(int chordId)</code> - checks if a servent with that
 * Chord ID is already active.</li>
 * <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 * <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then
 * return it, otherwise returns the nearest predecessor for this key from my
 * successor table.</li>
 * <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor
 * table.</li>
 * <li><code>putValue(int key, int value)</code> - stores the value locally or
 * sends it on further in the system.</li>
 * <li><code>getValue(int key)</code> - gets the value locally, or sends a
 * message to get it from somewhere else.</li>
 * </ul>
 * 
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(String str) {
		return Math.abs(str.hashCode() % CHORD_SIZE);
	}

	private int chordLevel; // log_2(CHORD_SIZE)

	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;

	// we DO NOT use this to send messages, but only to construct the successor
	// table
	private List<ServentInfo> allNodeInfo;

	private Map<Integer, File> valueMap;
	
	
	

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { // not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}

		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message. It sets
	 * up our initial value map and our first successor so we can send
	 * <code>UPDATE</code>. It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo(welcomeMsg.getSenderIp(), welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();

		// tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP, AppConfig.BOOTSTRAP_PORT);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			String toSend = AppConfig.myServentInfo.getListenerPort() + "," + AppConfig.myServentInfo.getIpAddress();
			bsWriter.write("New\n" + toSend + "\n");

			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getChordLevel() {
		return chordLevel;
	}

	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public String getNextNodeIp() {
		return successorTable[0].getIpAddress();
	}

	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}

	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, File> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, File> valueMap) {
		this.valueMap = valueMap;
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}

		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		if (predecessorChordId < myChordId) { // no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { // overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Main chord operation - find the nearest node to hop to to find a specific
	 * key. We have to take a value that is smaller than required to make sure we
	 * don't overshoot. We can only be certain we have found the required node when
	 * it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}

		// normally we start the search from our first successor
		int startInd = 0;

		// if the key is smaller than us, and we are not the owner,
		// then all nodes up to CHORD_SIZE will never be the owner,
		// so we start the search from the first item in our table after CHORD_SIZE
		// we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}

		int previousId = successorTable[startInd].getChordId();

		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}

			int successorId = successorTable[i].getChordId();

			if (successorId >= key) {
				return successorTable[i - 1];
			}
			if (key > previousId && successorId < previousId) { // overflow
				return successorTable[i - 1];
			}
			previousId = successorId;
		}
		// if we have only one node in all slots in the table, we might get here
		// then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		// first node after me has to be successorTable[0]

		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;

		int currentIncrement = 2;

		ServentInfo previousNode = AppConfig.myServentInfo;

		// i is successorTable index
		for (int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			// we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();

			// this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					// before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						// try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { // node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					// check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						// try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by
	 * chordId, starting from this node. Once the list is created, we invoke
	 * <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);

		allNodeInfo.sort(new Comparator<ServentInfo>() {

			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}

		});

		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();

		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}

		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size() - 1);
		} else {
			predecessorInfo = newList.get(newList.size() - 1);
		}

		updateSuccessorTable();
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it
	 * on.
	 */
	public void putValue(int hashFileName, String fileName, String content, String extension, String relativePath, boolean isDir) {
		if (isKeyMine(hashFileName)) {

			if (valueMap.get(hashFileName) != null) {
				System.out.print("I already have file with name: " + fileName);
				return;
			}
			
			String storage = AppConfig.STORAGE_PATH + File.separator;
			if(!isDir) {
				
				relativePath = relativePath.replace("\\", "/");
				String[] splitPath = relativePath.split("/");
				
				if(splitPath.length > 1) {
					for(int i=0; i<splitPath.length-1; i++) {
						File dir = new File(storage + splitPath[i]);
						if (!dir.exists()) {
							dir.mkdir();
						}
						System.out.println(storage);
						storage+= splitPath[i] + File.separator;
					}
				}
			}		

			File newFile = new File(storage+ fileName + "_0" + extension);
			
			try {
				newFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(newFile.getAbsolutePath()))) {
				bw.write(content);
				bw.newLine();
				bw.close();
			} catch (FileNotFoundException e) {
				System.out.println("Error BR: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Error BW: " + e.getMessage());
			}

			valueMap.put(hashFileName, newFile);
		} else {
			ServentInfo nextNode = getNextNodeForKey(hashFileName);
			AddMessage pm = new AddMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					hashFileName, fileName, content, extension, relativePath, isDir);
			MessageUtil.sendMessage(pm);
		}
	}

	private String getFileContent(String fileName, File file) {
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
			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public List<File> getFileOrFolderWithHash(int hashname, int version){
		
		return null;
	}
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise
	 * asks someone else to give us the value.
	 * 
	 * @return
	 *         <ul>
	 *         <li>The value, if we have it</li>
	 *         <li>-1 if we own the key, but there is nothing there</li>
	 *         <li>-2 if we asked someone else</li>
	 *         </ul>
	 */
	public List<File> getValue(int hashFileName, int version) {
		if (isKeyMine(hashFileName)) {
			List<File> toReturn = new ArrayList<>();
			if (valueMap.containsKey(hashFileName)) {
				File storage = new File(AppConfig.STORAGE_PATH);
				File[] files = storage.listFiles();
				int max = 0;
				for (int i = 0; i < files.length; i++) {
					String fileName = getFileName(files[i]);
					
					if(files[i].isDirectory() && (chordHash(files[i].getName()) == hashFileName)) {
						List<File> retList = (List<File>) Arrays.asList(files[i].listFiles()).stream().filter(e -> !e.isDirectory());
						System.out.print(retList.toString());
						return retList;
					}
//					if (chordHash(fileName) == hashFileName) {
//						int tmpVersion = getFileVersion(files[i]);
//						if (version == -1) {
//							if (tmpVersion > max) {
//								max = tmpVersion;
//								toReturn = files[i];
//							}
//						} else {
//							if (version == tmpVersion)
//								toReturn = files[i];
//						}
//					}
				}
				return toReturn;

			} else {
				return null;
			}
		}

		ServentInfo nextNode = getNextNodeForKey(hashFileName);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(),
				AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
				String.valueOf(hashFileName+","+version));
		MessageUtil.sendMessage(agm);

		return null;
	}

	private String getFileName(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return file.getName().substring(0, name.length() - 2); // empty extension
		}
		return name.substring(0, lastIndexOf - 2);
	}

	private Integer getFileVersion(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		int lastIndexOfUderscore = name.lastIndexOf("_");
		if (lastIndexOf == -1) {
			return Integer.parseInt(file.getName().substring(lastIndexOfUderscore + 1, name.length())); // empty
																										// extension
		}
		if(lastIndexOfUderscore == -1) {
			return -1;
		}
		return Integer.parseInt(name.substring(lastIndexOfUderscore + 1, lastIndexOf));
	}

}
