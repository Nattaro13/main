package taskey.parser;

import taskey.messenger.ProcessedObject;
import taskey.messenger.Task;
import taskey.constants.ParserConstants;

/**
 * @@author A0107345L
 * Purpose of this class is to parse the "delete" command
 * @author Xue Hui
 *
 */
public class ParseDelete extends ParseCommand {
	
	public ParseDelete() {
		super(); 
	}
	
	/**
	 * If command is delete, check if the deletion is by 
	 * 1. NAME, or
	 * 2. INDEX 
	 * 3. By Category (either by user defined tags or basic categories
	 * and return the appropriate ProcessedObject
	 * @param command
	 * @param stringInput
	 * @return appropriate ProcessedObject 
	 */
	protected ProcessedObject processDelete(String stringInput) {
		assert(stringInput != null); 
		
		ProcessedObject processed; 
		String taskName = getTaskName(stringInput);
		
		if (taskName.compareTo("") == 0) {
			return super.processError(ParserConstants.ERROR_DEL_EMPTY); 
		}
		
		//delete by category
		if (taskName.contains("#")) {
			return handleDeleteByCategory(taskName);
		}
		
		//handle delete by index of delete by task name
		processed = handleSingleDelete(taskName);
		
		return processed; 
	}
	
	/**
	 * Handle deleting by a single index or a single task name
	 * @param taskName
	 * @return ProcessedObject
	 */
	private ProcessedObject handleSingleDelete(String taskName) {
		ProcessedObject processed;
		try {
			//delete by index
			int index = Integer.parseInt(taskName);
			processed = new ProcessedObject(ParserConstants.DELETE_BY_INDEX, index-1); 
			
		} catch (Exception e) {
			//if the delete is not by index, then it's by task name. 
			processed = new ProcessedObject(ParserConstants.DELETE_BY_NAME, new Task(taskName));
		}
		return processed;
	}

	/**
	 * Process the deleting by category
	 * @param taskName
	 * @return ProcessedObject
	 */
	private ProcessedObject handleDeleteByCategory(String taskName) {
		ProcessedObject processed;
		String[] splitArr = taskName.split("#");
		if (splitArr.length > 1) { 
			String category = splitArr[1].trim(); 
			processed = new ProcessedObject(ParserConstants.DELETE_BY_CATEGORY); 
			processed.setCategory(category);
			return processed; 
		} else {
			return super.processError(ParserConstants.ERROR_DEL_EMPTY_CAT); 
		}
	}
	
	/**
	 * Given a stringInput, remove the command from the string
	 * @param command
	 * @param stringInput
	 * @return taskName without command
	 */
	private String getTaskName(String stringInput) {
		String command = stringInput.split(" ")[0]; 
		String task = stringInput.replaceFirst(command, "");
		
		return task.trim(); 
	}
}
