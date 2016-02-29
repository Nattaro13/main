package taskey.logic;

import taskey.parser.Parser;
import taskey.storage.Storage;
import taskey.ui.UiManager;
import taskey.logic.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import taskey.logic.ProcessedObject;

/**
 * TODO: class description
 * 
 * @author Hubert Wong
 */
public class Logic {
	/* List of status codes. Other components like Ui can use Logic.statusCode to access this list. */
	public static final int SUCCESS_VIEW = 1;
	public static final int SUCCESS_ADD = 2;
	public static final int SUCCESS_DELETE = 3;
	public static final int SUCCESS_UPDATE = 4;
	public static final int SUCCESS_DONE = 5;
	public static final int SUCCESS_SEARCH = 6;
	public static final int SUCCESS_UNDO = 7;
	public static final int ERROR_VIEW = -1;
	public static final int ERROR_ADD = -2;
	public static final int ERROR_DELETE = -3;
	public static final int ERROR_UPDATE = -4;
	public static final int ERROR_DONE = -5;
	public static final int ERROR_SEARCH = -6;
	public static final int ERROR_UNDO = -7;
	
	private static Logic instance = null;
	private Parser parser;
	private Storage storage;
	private UiManager uiManager;
	
	//The most recent processed object whose command is not VIEW, UNDO, SEARCH or ERROR
	private ProcessedObject mostRecentProcessedObject = null;
	
	//The current view type that Ui is displaying, e.g. deadline, events
	private String uiCurrentViewType = null;
	
	//Task lists retrieved from Storage at startup 
	private ArrayList<ArrayList<Task>> listsFromStorage = null;
	
	//Number of task lists in listsFromStorage
	private static final int NUM_TASK_LISTS = 6;
	
	//Indices of each Task list in listsFromStorage
	private static final int INDEX_ALL_LIST = 0;
	private static final int INDEX_FLOATING_LIST = 1;
	private static final int INDEX_DEADLINE_LIST = 2;
	private static final int INDEX_EVENT_LIST = 3;
	private static final int INDEX_DONE_LIST = 4;
	private static final int INDEX_EXPIRED_LIST = 5;
	
	//Names of save file for each Task list. Can be moved to Storage later on.
	private static final String NAME_ALL_SAVE_FILE = "all tasks";
	private static final String NAME_FLOATING_SAVE_FILE = "floating tasks";
	private static final String NAME_DEADLINE_SAVE_FILE = "deadline tasks";
	private static final String NAME_EVENT_SAVE_FILE = "event tasks";
	private static final String NAME_DONE_SAVE_FILE = "done tasks";
	private static final String NAME_EXPIRED_SAVE_FILE = "expired tasks";
	
	//HashMaps containing Task data for each Task category. 
	//The key String holds the name of Task, and the value Task is the corresponding Task object.
	private HashMap<String, Task> allMap = null;
	private HashMap<String, Task> floatingMap = null;
	private HashMap<String, Task> deadlineMap = null;
	private HashMap<String, Task> eventMap = null;
	private HashMap<String, Task> doneMap = null;
	private HashMap<String, Task> expiredMap = null;
	
	//Collections of Task objects backed by the above HashMaps 
	private ArrayList<Task> allCollection = null; 
	private ArrayList<Task> floatingCollection = null;
	private ArrayList<Task> deadlineCollection = null;
	private ArrayList<Task> eventCollection = null;
	private ArrayList<Task> doneCollection = null;
	private ArrayList<Task> expiredCollection = null;
	
	/**
	 * Gets an instance of the Logic class if an instance does not already exist.
	 * 
	 * @return an instance of the Logic class
	 */
	public static Logic getInstance() {
		if (instance == null) {
    		instance = new Logic();
    		//instance.parser = Parser.getInstance();
    		instance.storage = Storage.getInstance();
    		instance.uiManager = UiManager.getInstance();
    	}
    	return instance;
    }
	

	
    /**
     * Attempts to execute a command specified by the input string.
     * 
     * @param input the input string
     * @return      status code reflecting the outcome of command execution
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public int executeCommand(String input) throws ClassNotFoundException, IOException {
    	int statusCode = 0; //Stub
    	ProcessedObject po = parser.parseInput(input);
    	String command = po.getCommand();
    	Task task = po.getTask();
    	int taskIndex = po.getIndex() - 1; //Only used for commands that specify the index of a task
    	String viewType = po.getViewType(); //Only used for view commands
    	String errorType = po.getErrorType(); //Only used for invalid commands
    	String searchPhrase = po.getSearchPhrae(); //Only used for search commands
    	String newTaskName = po.getNewTaskName(); //Only used for commands that change the name of a task
    	String taskName = task.getTaskName();
   	
    	switch (command) {
    		case "VIEW":
    			statusCode = view(viewType);
    			break;
    			
    		case "ADD_FLOATING":
    			statusCode = addFloatingToStorage(task, taskName);
    			break;
    			
    		case "ADD_DEADLINE":
    			statusCode = addDeadlineToStorage(task, taskName);
    			break;
    			
    		case "ADD_EVENT":
    			statusCode = addEventToStorage(task, taskName);
    			break;
    			
    		case "DELETE_BY_INDEX":
    			statusCode = deleteIndexedTaskFromStorage(taskIndex);
    			break;
    		
    		case "DELETE_BY_NAME":
    			statusCode = deleteNamedTaskFromStorage(taskName);
    			break;
    		
    		case "UPDATE_BY_INDEX_CHANGE_NAME":
    			statusCode = updateIndexedTaskNameInStorage(taskIndex, newTaskName);
    			break;
    			
    		case "UPDATE_BY_INDEX_CHANGE_DATE":
    			statusCode = updateIndexedTaskDateInStorage(task, taskIndex);
    			break;
    		
    		case "UPDATE_BY_NAME_CHANGE_NAME":
    			statusCode = updateNamedTaskNameInStorage(taskName, newTaskName);
    			break;
    			
    		case "UPDATE_BY_NAME_CHANGE_DATE":
    			statusCode = updateNamedTaskDateInStorage(task, taskName);
    			break;
    				
    		case "DONE_BY_INDEX":
    			statusCode = markIndexedTaskAsDoneInStorage(taskIndex);
    			break;
    		
    		case "DONE_BY_NAME":
    			statusCode = markNamedTaskAsDoneInStorage(taskName);
    			break;
    			
    		case "SEARCH":
    			Task t = search(searchPhrase);
    			if (t == null) { //Task not found
    				statusCode = -1; //Stub
    			} else {
    				statusCode = -1; //Stub
    			}
    			break;
    		
    		case "UNDO":
    			statusCode = undo();
    			break;
    		
    		case "ERROR":
    			switch (errorType) {
    				case "ERROR_COMMAND":
    					//TODO: pass message to Ui
    					break;
    				
    				case "ERROR_VIEW_TYPE":
    					//TODO: pass message to Ui
    					break;
    				
    				case "ERROR_DATE_FORMAT":
    					//TODO: pass message to Ui
    					break;
    				
    				default:
    			}
    			
    		default:
    	}
    	
    	if (isUndoableCommand(command)) {
    		mostRecentProcessedObject = po;
    	}
    	uiManager.updateDisplay();
    	return statusCode; 
    }
    
    //Updates Ui with a list of Tasks sorted by date, corresponding to the view type.
    //Returns a status code representing outcome of action.
    private int view(String viewType) throws ClassNotFoundException, IOException {
    	int statusCode = -1; //Stub 
    	
    	if (allMap == null) { //HashMap not initialized at startup, must get Tasks from Storage
			statusCode = getListsFromStorage();
			uiCurrentViewType = "ALL";
			//TODO: update Ui with allCollection
		} else if (viewType.equals("ALL")) {
			Collections.sort(allCollection);
			//TODO: update Ui with allCollection
		} else if (viewType.equals("GENERAL")) {
			Collections.sort(floatingCollection);
			//TODO: update Ui with floatingCollection
		} else if (viewType.equals("DEADLINES")) {
			Collections.sort(deadlineCollection);
			//TODO: update Ui with deadlineCollection
		} else if (viewType.equals("EVENTS")) {
			Collections.sort(eventCollection);
			//TODO: update Ui with eventCollection
		} else if (viewType.equals("DONE")) {
			Collections.sort(doneCollection);
			//TODO: update Ui with doneCollection
		} else { //Expired tasks
			Collections.sort(expiredCollection);
			//TODO: update Ui with expiredCollection
		}
    	uiCurrentViewType = viewType;
    	
    	return statusCode; 
    }
    
    //Add the floating Task to Storage. Returns a status code representing outcome of action.
    private int addFloatingToStorage(Task task, String taskName) throws IOException {
		floatingMap.put(taskName, task); 
		storage.saveTaskList(floatingCollection, NAME_FLOATING_SAVE_FILE);	
		allMap.put(taskName, task);
		storage.saveTaskList(allCollection, NAME_ALL_SAVE_FILE);
		
		return -1; //Stub
    }
    
    //Add the deadline Task to Storage. Returns a status code representing outcome of action.
    private int addDeadlineToStorage(Task task, String taskName) throws IOException {
		deadlineMap.put(taskName, task); 
		storage.saveTaskList(deadlineCollection, NAME_DEADLINE_SAVE_FILE);	
		allMap.put(taskName, task);
		storage.saveTaskList(allCollection, NAME_ALL_SAVE_FILE);
		
		return -1; //Stub
    }
    
  //Add the event Task to Storage. Returns a status code representing outcome of action.
    private int addEventToStorage(Task task, String taskName) throws IOException {
		eventMap.put(taskName, task); 
		storage.saveTaskList(eventCollection, NAME_EVENT_SAVE_FILE);	
		allMap.put(taskName, task);
		storage.saveTaskList(allCollection, NAME_ALL_SAVE_FILE);
		
		return -1; //Stub
    }
    
    //Undo the most recent action that was not view, undo, search or error.
    //Returns a status code representing outcome of action.
	private int undo() throws IOException {
		if (mostRecentProcessedObject == null) { //No undoable tasks since startup
			return ERROR_UNDO; //Stub
		}
		String mostRecentCommand = mostRecentProcessedObject.getCommand();
		Task mostRecentTask = mostRecentProcessedObject.getTask();
		String mostRecentTaskType = mostRecentTask.getTaskType();
		String mostRecentTaskName = mostRecentTask.getTaskName();
		switch (mostRecentCommand) {
			case "ADD_FLOATING":
				floatingMap.remove(mostRecentTaskName); 
				storage.saveTaskList(floatingCollection, "floating tasks");
				allMap.remove(mostRecentTaskName);
				storage.saveTaskList(allCollection, "all tasks");
				break;
				
			case "ADD_DEADLINE":
				deadlineMap.remove(mostRecentTaskName);
				storage.saveTaskList(deadlineCollection, "deadline tasks");
				allMap.remove(mostRecentTaskName);
				storage.saveTaskList(allCollection, "all tasks");
				break;
				
			case "ADD_EVENT":
				eventMap.remove(mostRecentTaskName);
				storage.saveTaskList(eventCollection, "event tasks");
				allMap.remove(mostRecentTaskName);
				storage.saveTaskList(allCollection, "all tasks");
				break;
			
			case "DELETE_BY_INDEX":
			case "DELETE_BY_NAME":
				if (mostRecentTaskType == "FLOATING") {
					floatingMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(floatingCollection, "floating tasks");
				} else if (mostRecentTaskType == "EVENT") {
					eventMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(eventCollection, "event tasks");
				} else { //Deadline tasks
					deadlineMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(deadlineCollection, "deadline tasks");
				}
				allMap.put(mostRecentTaskName, mostRecentTask);
				storage.saveTaskList(allCollection, "all tasks");
				break;
			
			case "UPDATE_BY_INDEX":
			case "UPDATE_BY_NAME":
				//TODO: revert most recently updated task in storage
				break;
				
			case "DONE_BY_INDEX":
			case "DONE_BY_NAME":
				if (mostRecentTaskType == "FLOATING") {
					floatingMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(floatingCollection, "floating tasks");
				} else if (mostRecentTaskType == "EVENT") {
					eventMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(eventCollection, "event tasks");
				} else { //Deadline tasks
					deadlineMap.put(mostRecentTaskName, mostRecentTask);
					storage.saveTaskList(deadlineCollection, "deadline tasks");
				}				
				doneMap.remove(mostRecentTaskName);
				storage.saveTaskList(doneCollection, "done tasks");				
				allMap.put(mostRecentTaskName, mostRecentTask);
				storage.saveTaskList(allCollection, "all tasks");				
				break;
			
			default:
		}
		
		return -1; //Stub
	}
	
	//Deletes the Task specified by taskIndex from Storage.
	//Returns a status code reflecting outcome of command execution.
	private int deleteIndexedTaskFromStorage(int taskIndex) throws IOException {
		Task toDelete = getIndexedTask(taskIndex);
		
		if (toDelete == null) { //Index is invalid
			return -1; //Stub
		}
		
		String toDeleteType = toDelete.getTaskType();
		String toDeleteName = toDelete.getTaskName();
		
		return removeTaskFromMaps(toDeleteName, toDeleteType);
	}
	
	//Deletes the Task specified by taskName from Storage.
	//Returns a status code reflecting outcome of command execution.
	private int deleteNamedTaskFromStorage(String taskName) throws IOException {
		if (allMap.containsKey(taskName)) {
			Task toDelete = allMap.get(taskName);
			String taskType = toDelete.getTaskType();
			return removeTaskFromMaps(taskName, taskType);
		} else { //Task to delete does not exist
			return -1; //Stub
		}
	}
	
	//Updates the Task specified by taskIndex in Storage with newTaskName.
	//Returns a status code reflecting outcome of command execution.
	private int updateIndexedTaskNameInStorage(int taskIndex, String newTaskName) throws IOException {
		Task toUpdate = getIndexedTask(taskIndex);
		
		if (toUpdate == null) {
			return -1; //Stub
		}
		
		String toUpdateType = toUpdate.getTaskType();
		String toUpdateName = toUpdate.getTaskName();
		removeTaskFromMaps(toUpdateName, toUpdateType);
		toUpdate.setTaskName(newTaskName);
		
		return putTaskInMaps(toUpdate, newTaskName, toUpdateType);
	}
	
	//Updates the Task specified by taskIndex in Storage with task which contains the new date.
	//Returns a status code reflecting outcome of command execution.
	private int updateIndexedTaskDateInStorage(Task task, int taskIndex) throws IOException {
		Task toUpdate = getIndexedTask(taskIndex);
		
		if (toUpdate == null) {
			return -1; //Stub
		}
		
		String toUpdateType = toUpdate.getTaskType();
		String toUpdateName = toUpdate.getTaskName();
		String newTaskType = task.getTaskType();
		removeTaskFromMaps(toUpdateName, toUpdateType);
		task.setTaskName(toUpdateName);
		
		return putTaskInMaps(task, toUpdateName, newTaskType);
	}
	
	//Updates the Task specified by taskName in Storage with newTaskName.
	//Returns a status code reflecting outcome of command execution.
	private int updateNamedTaskNameInStorage(String oldTaskName, String newTaskName) throws IOException {
		if (allMap.containsKey(oldTaskName)) {
			Task toUpdate = allMap.get(oldTaskName);
			String toUpdateType = toUpdate.getTaskType();
			removeTaskFromMaps(oldTaskName, toUpdateType);
			toUpdate.setTaskName(newTaskName);
			return putTaskInMaps(toUpdate, newTaskName, toUpdateType);
		} else { //Task to update does not exist
			return -1; //Stub
		}
	}
	
	//Updates the Task specified by taskName in Storage with task which contains the new date.
	//Returns a status code reflecting outcome of command execution.
	private int updateNamedTaskDateInStorage(Task task, String taskName) throws IOException {
		if (allMap.containsKey(taskName)) {
			Task toUpdate = allMap.get(taskName);
			String toUpdateType = toUpdate.getTaskType();
			String newTaskType = task.getTaskType();
			removeTaskFromMaps(taskName, toUpdateType);
			return putTaskInMaps(task, taskName, newTaskType);
		} else { //Task to update does not exist
			return -1; //Stub
		}
	}
	
	//Marks the Task specified by taskIndex as done in Storage.
	//Returns a status code reflecting outcome of command execution.
	private int markIndexedTaskAsDoneInStorage(int taskIndex) throws IOException {
		Task toMark = getIndexedTask(taskIndex);
			
		if (toMark == null) { //Index is invalid
			return -1; //Stub
		}
			
		String toMarkType = toMark.getTaskType();
		String toMarkName = toMark.getTaskName();
		removeTaskFromMaps(toMarkName, toMarkType);
		doneMap.put(toMarkName, toMark);
		storage.saveTaskList(doneCollection, NAME_DONE_SAVE_FILE);
			
		return -1; //Stub
	}
	
	//Marks the Task specified by taskName as done in Storage.
	//Returns a status code reflecting outcome of command execution.
	private int markNamedTaskAsDoneInStorage(String taskName) throws IOException {
		if (allMap.containsKey(taskName)) {
			Task toMark= allMap.get(taskName);
			String toMarkName = toMark.getTaskName();
			String toMarkType = toMark.getTaskType();
			removeTaskFromMaps(taskName, toMarkType);
			doneMap.put(toMarkName, toMark);
			storage.saveTaskList(doneCollection, NAME_DONE_SAVE_FILE);
			return -1; //Stub
		} else { //Task name does not exist
			return -1; //Stub
		}
	}
	
	//Returns an existing Task whose name matches searchPhrase, or null if no matches are found.
	private Task search(String searchPhrase) {
		if (allMap.containsKey(searchPhrase)) { //Only works if searchPhrase matches taskName
			return allMap.get(searchPhrase);
			//TODO: update Ui with Task
		} else {
			return null;
		}
	}
	
	//Adds the specified <taskName, task> mappings to the relevant HashMaps determined by taskType.
	//This method also saves the updated Task collections to Storage.
	//Returns a status code reflecting outcome of command execution.
	private int putTaskInMaps(Task task, String taskName, String taskType) throws IOException {
		if (taskType.equals("FLOATING")) {
			floatingMap.put(taskName, task);
			storage.saveTaskList(floatingCollection, NAME_FLOATING_SAVE_FILE);
		} else if (taskType.equals("DEADLINE")) {
			deadlineMap.put(taskName, task);
			storage.saveTaskList(deadlineCollection, NAME_DEADLINE_SAVE_FILE);
		} else if (taskType.equals("EVENT")) {
			eventMap.put(taskName, task);
			storage.saveTaskList(eventCollection, NAME_EVENT_SAVE_FILE);
		}
		
		allMap.put(taskName, task);
		storage.saveTaskList(allCollection, NAME_ALL_SAVE_FILE);
		
		return -1; //stub
	}
	
	//Removes the mappings whose keys are specified by taskName from the relevant HashMaps.
	//The HashMaps to remove the mappings from are determined by taskType.
	//This method also saves the updated Task collections to Storage.
	//Returns a status code reflecting outcome of command execution.
	private int removeTaskFromMaps(String taskName, String taskType) throws IOException {
		if (taskType.equals("FLOATING")) {
			floatingMap.remove(taskName);
			storage.saveTaskList(floatingCollection, NAME_FLOATING_SAVE_FILE);
		} else if (taskType.equals("DEADLINE")) {
			deadlineMap.remove(taskName);
			storage.saveTaskList(deadlineCollection, NAME_DEADLINE_SAVE_FILE);
		} else if (taskType.equals("EVENT")) {
			eventMap.remove(taskName);
			storage.saveTaskList(eventCollection, NAME_EVENT_SAVE_FILE);
		}
		
		allMap.remove(taskName);
		storage.saveTaskList(allCollection, NAME_ALL_SAVE_FILE);
		
		return -1; //stub
	}
	
	//Returns a reference to the indexed Task based on the current Ui view type, or null if index is invalid.
	private Task getIndexedTask(int taskIndex) {
		if (outOfBounds(taskIndex)) {
			return null;
		}
		
		if (uiCurrentViewType.equals("ALL")) {
			return allCollection.get(taskIndex);
		} else if (uiCurrentViewType.equals("FLOATING")) {
			return floatingCollection.get(taskIndex);
		} else if (uiCurrentViewType.equals("DEADLINE")) {
			return deadlineCollection.get(taskIndex);
		} else if (uiCurrentViewType.equals("EVENT")) {
			return eventCollection.get(taskIndex);
		} else { //May need to add more cases like "EXPIRED", "DONE" etc.
			return null;
		}
	}
	
	//Determines if the given Task index is valid for the current Ui view type.
	//Returns true if the index is out of bounds (invalid).
	private boolean outOfBounds(int taskIndex) {
		if (uiCurrentViewType.equals("ALL")) {
			return (taskIndex < allCollection.size()) ? false : true;
		} else if (uiCurrentViewType.equals("FLOATING")) {
			return (taskIndex < floatingCollection.size()) ? false : true;
		} else if (uiCurrentViewType.equals("DEADLINE")) {
			return (taskIndex < deadlineCollection.size()) ? false : true;
		} else if (uiCurrentViewType.equals("EVENT")) {
			return (taskIndex < eventCollection.size()) ? false : true;
		} else { //May need to add more cases like "EXPIRED", "DONE" etc.
			return false;
		}
	}
	
	//Get Task lists from Storage at startup and populate the HashMaps and their corresponding collections.
    //Returns a status code representing outcome of action.
    private int getListsFromStorage() throws IOException, ClassNotFoundException {
    	listsFromStorage = new ArrayList<ArrayList<Task>>(NUM_TASK_LISTS);
    	
    	//Get ALL list from Storage
    	listsFromStorage.set(INDEX_ALL_LIST, storage.getTaskList(NAME_ALL_SAVE_FILE));
    	allMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_ALL_LIST)) {
    		allMap.put(t.getTaskName(), t);
    	}
    	allCollection = (ArrayList<Task>) allMap.values();
    	
    	//Get FLOATING list from Storage
    	listsFromStorage.set(INDEX_FLOATING_LIST, storage.getTaskList(NAME_FLOATING_SAVE_FILE));
    	floatingMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_FLOATING_LIST)) {
    		floatingMap.put(t.getTaskName(), t);
    	}
    	floatingCollection = (ArrayList<Task>) floatingMap.values();
    	
    	//Get DEADLINE list from Storage
    	listsFromStorage.set(INDEX_DEADLINE_LIST, storage.getTaskList(NAME_DEADLINE_SAVE_FILE));
    	deadlineMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_DEADLINE_LIST)) {
    		deadlineMap.put(t.getTaskName(), t);
    	}
    	deadlineCollection = (ArrayList<Task>) deadlineMap.values();
    	
    	//Get EVENT list from Storage
    	listsFromStorage.set(INDEX_EVENT_LIST, storage.getTaskList(NAME_EVENT_SAVE_FILE));
    	eventMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_EVENT_LIST)) {
    		eventMap.put(t.getTaskName(), t);
    	}
    	eventCollection = (ArrayList<Task>) eventMap.values();
    	
    	//Get DONE list from Storage
    	listsFromStorage.set(INDEX_DONE_LIST, storage.getTaskList(NAME_DONE_SAVE_FILE));
    	doneMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_DONE_LIST)) {
    		doneMap.put(t.getTaskName(), t);
    	}
    	doneCollection = (ArrayList<Task>) doneMap.values();
    	
    	//Get EXPIRED list from Storage
    	listsFromStorage.set(INDEX_EXPIRED_LIST, storage.getTaskList(NAME_EXPIRED_SAVE_FILE));
    	expiredMap = new HashMap<String, Task>();
    	for (Task t : listsFromStorage.get(INDEX_EXPIRED_LIST)) {
    		expiredMap.put(t.getTaskName(), t);
    	}
    	expiredCollection = (ArrayList<Task>) expiredMap.values();
    	
    	return -1; //Stub
    }
	
    //Returns true if the supplied command can be undone.
    private boolean isUndoableCommand(String command) {
    	switch (command) {
    		case "VIEW":
    		case "SEARCH":
    		case "UNDO":
    		case "ERROR":
    			return false;
    		
    		default:
    	}
    	
    	return true;
    }
}