package app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.ConflictObject;
import servent.handler.ConflictHandler;
import servent.message.AddMessage;
import servent.message.AskPullMessage;
import servent.message.CommitMessage;
import servent.message.PushMessage;
import servent.message.RemoveMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

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
	public List<ServentInfo> allNodeInfo;

	private Map<Integer, ArrayList<File>> valueMap;
	private Map<String, Integer> fileVersions;
	private Map<Integer, List<Integer>> childrenHashes;

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
		childrenHashes = new HashMap<>();
		allNodeInfo = new ArrayList<>();
		fileVersions = new HashMap<>();
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message. It sets
	 * up our initial value map and our first successor so we can send
	 * <code>UPDATE</code>. It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo(welcomeMsg.getSenderIp(), welcomeMsg.getSenderPort());
		// System.out.println(childrenHashes.toString());
		this.valueMap = welcomeMsg.getValues();
		this.childrenHashes = welcomeMsg.getChildrenHashes();

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

	public void setAllNodeInfo(List<ServentInfo> allNodeInfo) {
		this.allNodeInfo = allNodeInfo;
	}

	public Map<Integer, List<Integer>> getChildrenHashes() {
		return childrenHashes;
	}

	public void setChildrenHashes(Map<Integer, List<Integer>> childrenHashes) {
		this.childrenHashes = childrenHashes;
	}

	public Map<String, Integer> getFileVersions() {
		return fileVersions;
	}

	public int getChordLevel() {
		return chordLevel;
	}

	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public ServentInfo getNextNode() {
		return successorTable[0];
	}

	public Integer getNextNodePort() {
		if (successorTable[0] == null)
			return null;
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

	public Map<Integer, ArrayList<File>> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, ArrayList<File>> valueMap) {
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
		System.out.println(" predecesor " + predecessorChordId + " ja sam " + myChordId);
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

	public void updateSuccessorTable() {
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
		System.out.println("nova lista sledbenika " + successorTable.toString());
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
		System.out.println("All node info " + allNodeInfo.toString());
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
	public void putValue(int hashFileName, String fileName, String content, String extension, String relativePath,
			boolean isDir, ArrayList<String> children) {

		if (isKeyMine(hashFileName)) {

			String storage = AppConfig.STORAGE_PATH + File.separator;
			if (!isDir) {

				relativePath = relativePath.replace("\\", "/");
				String[] splitPath = relativePath.split("/");

				if (splitPath.length > 1) {
					for (int i = 0; i < splitPath.length - 1; i++) {
						File dir = new File(storage + splitPath[i]);
						if (!dir.exists()) {
							dir.mkdir();
						}
						storage += splitPath[i] + File.separator;
					}
				}
				File newFile = new File(storage + fileName + "_0" + extension);

				try {
					newFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				AppConfig.fileConfig.setFileContent(newFile, content);
				ArrayList<File> list = new ArrayList<File>();
				list.add(newFile);
				valueMap.put(hashFileName, list);
			} else {

				System.out.println("hocu da napravim dir " + relativePath);
				relativePath = relativePath.replace("\\", "/");
				String[] splitPath = relativePath.split("/");

				File newFile = new File(storage + splitPath[splitPath.length - 1]);

				if (!newFile.exists()) {
					newFile.mkdir();
				}

				ArrayList<File> list = new ArrayList<File>();
				list.add(newFile);
				valueMap.put(hashFileName, list);

				ArrayList<Integer> childrenHash = new ArrayList<>();

				for (String child : children) {
					childrenHash.add(chordHash(child));
				}

				childrenHashes.put(hashFileName, childrenHash);
			}

			AppConfig.releaseBothMutex();

		} else {
			ServentInfo nextNode = getNextNodeForKey(hashFileName);
			AddMessage pm = new AddMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					hashFileName, fileName, content, extension, relativePath, isDir, children,
					AppConfig.myServentInfo.getChordId());
			MessageUtil.sendMessage(pm);
		}
	}

	public void commit(int hashFileName, String fileName, String content, String extension, boolean isDir,
			ArrayList<String> children, int version) {

		if (isKeyMine(hashFileName)) {

			File storage = new File(AppConfig.STORAGE_PATH + File.separator);
			File[] files = storage.listFiles();
			int max = -1;
			File maxVersion = null;

			for (int i = 0; i < files.length; i++) {

				String absolutePath1 = AppConfig.fileConfig.getFileNameWithoutVersion(files[i].getAbsolutePath());
				String absolutePath2 = storage.getAbsolutePath() + "\\";
				String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

				if (!relative.equals("")) {

					if (ChordState.chordHash(relative) == hashFileName) {

						int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);

						if (versionOfFile > max) {
							max = versionOfFile;
							maxVersion = files[i];
						}
					}
				}
			}

			if (max > version) {

				ConflictObject conflictObject = new ConflictObject(max, AppConfig.fileConfig.getFileContent(maxVersion), fileName, extension);
				AppConfig.myServentInfo.getConflicts().put(hashFileName, conflictObject);
				System.out.println(AppConfig.myServentInfo.getConflicts().toString());
				System.out.println("You have a conflict for files: ");
				for (Entry<Integer, ConflictObject> valueEntry : AppConfig.myServentInfo.getConflicts().entrySet()) {
					System.out.println(
							AppConfig.fileConfig.getFileNameWithoutVersion(valueEntry.getValue().getFileName()));
				}

				System.out.println("Choose an option: view [file], pull_conflict [file], push [file]");

			} else if (max != -1) {

				File oldFile = new File(AppConfig.STORAGE_PATH + File.separator + fileName + "_" + max + extension);

				String oldContent = AppConfig.fileConfig.getFileContent(oldFile);

				if (!oldContent.trim().equals(content.trim())) {
					int newVersion = max + 1;
					File commitFile = new File(
							AppConfig.STORAGE_PATH + File.separator + fileName + "_" + newVersion + extension);
					AppConfig.fileConfig.setFileContent(commitFile, content);
					AppConfig.chordState.getFileVersions().put(commitFile.getName(), newVersion);
					AppConfig.chordState.getValueMap().get(hashFileName).add(commitFile);
				}
			}

			System.out.println("The file you try to commit is deleted or doesn't exist!");
			AppConfig.releaseBothMutex();

		} else {
			ServentInfo nextNode = getNextNodeForKey(hashFileName);
			CommitMessage pm = new CommitMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					hashFileName, fileName, content, extension, version, isDir, children,
					AppConfig.myServentInfo.getChordId());
			MessageUtil.sendMessage(pm);
		}
	}

	public void push(int hashFileName, String fileName, String content, String extension, boolean isDir,
			ArrayList<String> children, int version) {

		if (isKeyMine(hashFileName)) {

			int newVersion = version + 1;
			File commitFile = new File(
					AppConfig.STORAGE_PATH + File.separator + fileName + "_" + newVersion + extension);

			AppConfig.fileConfig.setFileContent(commitFile, content);
			getValueMap().get(hashFileName).add(commitFile);

			fileVersions.put(fileName, newVersion);

			System.out.println("Push command succesfull!");
			AppConfig.releaseBothMutex();

		} else {
			ServentInfo nextNode = getNextNodeForKey(hashFileName);
			PushMessage pm = new PushMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					hashFileName, fileName, content, extension, version, isDir, children,
					AppConfig.myServentInfo.getChordId());
			MessageUtil.sendMessage(pm);
		}
	}

	public void getValue(ServentInfo target, int hash, int version) {
		File toReturn = null;
		if (AppConfig.chordState.isKeyMine(hash)) {
			if (valueMap.containsKey(hash)) {

				File storage = new File(AppConfig.STORAGE_PATH);
				File[] files = storage.listFiles();

				int max = -1;
				for (int i = 0; i < files.length; i++) {

					String absolutePath1 = AppConfig.fileConfig.getFileNameWithoutVersion(files[i].getAbsolutePath());
					String absolutePath2 = storage.getAbsolutePath() + "\\";
					String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

					if (!relative.equals("")) {

						if (ChordState.chordHash(relative) == hash) {

							if (!files[i].isDirectory()) {
								if (version == -1) {
									int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);

									if (versionOfFile > max) {
										max = versionOfFile;
										toReturn = files[i];
									}
								} else {
									int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);
									if (versionOfFile == version) {
										toReturn = files[i];

									}
								}
							} else {
								// ako pulujemo direktorijum
							}

						}
					}
				}

				if (toReturn != null) {
					String content = AppConfig.fileConfig.getFileContent(toReturn);
					int versionOfFile = AppConfig.fileConfig.getFileVersion(toReturn);
					String workRoute = AppConfig.WORK_ROUTE_PATH + File.separator;
					File newFile = new File(
							workRoute + AppConfig.fileConfig.getFileNameWithoutVersion(toReturn.getName())
									+ AppConfig.fileConfig.getFileExtension(toReturn));
					AppConfig.fileConfig.setFileContent(newFile, content);
					fileVersions.put(toReturn.getName(), versionOfFile);

				} else {
					System.out.println("Fajl nije pronadjen");

				}
			}else {
				System.out.println("File is deleted or doesn't exist!");
			}
			AppConfig.releaseBothMutex();

		} else {

			ServentInfo nextNode = getNextNodeForKey(hash);
			AskPullMessage agm = new AskPullMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					String.valueOf(hash + "," + version + "," + AppConfig.myServentInfo.getChordId()));
			MessageUtil.sendMessage(agm);

		}
	}

	public void remove(int hashFileName) {

		if (isKeyMine(hashFileName)) {

			File storage = new File(AppConfig.STORAGE_PATH + File.separator);
			File[] files = storage.listFiles();

			for (int i = 0; i < files.length; i++) {

				String absolutePath1 = AppConfig.fileConfig.getFileNameWithoutVersion(files[i].getAbsolutePath());
				String absolutePath2 = storage.getAbsolutePath() + "\\";
				String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

				if (!relative.equals("")) {

					if (ChordState.chordHash(relative) == hashFileName) {
						boolean ret = files[i].delete();
					}
				}
			}
			AppConfig.chordState.getValueMap().remove(hashFileName);
			AppConfig.releaseBothMutex();

		} else {
			ServentInfo nextNode = getNextNodeForKey(hashFileName);
			RemoveMessage rm = new RemoveMessage(AppConfig.myServentInfo.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
					hashFileName, AppConfig.myServentInfo.getChordId());
			MessageUtil.sendMessage(rm);
		}
	}

}
