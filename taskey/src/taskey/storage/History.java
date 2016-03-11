package taskey.storage;

import java.util.ArrayList;
import java.util.HashMap;

import taskey.logic.Task;

/**
 * This class is to allow Storage to easily retrieve the last successfully saved tasklist from memory
 * so that Storage can throw it to Logic when an error is encountered during saving.
 * This will in turn allow Logic to easily undo the last operation so that its data remains in sync with Storage.
 *
 * For now, this class is only meant to be used by Storage for the above purpose.
 * But, in the future, if more methods are added to this class, Logic could also use this for the undo command.
 * TODO: multiple undos/redos using stacks?
 *
 * @author Dylan
 */
public class History {
	private HashMap<FileType, ArrayList<Task>> lastSavedTasklists;

	public History() {
		lastSavedTasklists = new HashMap<FileType, ArrayList<Task>>();
	}

	/**
	 * Sets History to map the category specified by filename to tasklist.
	 * If the filename does not correspond to any type (is INVALID),
	 * the tasklist will not be added to History.
	 * @param filename category of the tasklist to be saved
	 * @param tasklist ArrayList of tasks to be saved
	 */
	public void set(String filename, ArrayList<Task> tasklist) {
		FileType tasklistCategory = FileType.getType(filename);
		if (tasklistCategory != FileType.INVALID) {
			lastSavedTasklists.put(tasklistCategory, tasklist);
		}
	}

	/**
	 * Gets the last-saved tasklist specified by filename.
	 * An empty ArrayList is returned if the tasklist specified by filename
	 * has not been added to History yet,
	 * or if the tasklist category specified by filename is invalid.
	 * @param filename category of the last-saved tasklist
	 * @return the last-saved tasklist specified by filename
	 */
	public ArrayList<Task> get(String filename) {
		FileType tasklistCategory = FileType.getType(filename);
		ArrayList<Task> ret = lastSavedTasklists.get(tasklistCategory);
		if (ret == null) {
			ret = new ArrayList<Task>();
		}
		return ret;
	}
}