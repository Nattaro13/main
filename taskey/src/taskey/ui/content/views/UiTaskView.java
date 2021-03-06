package taskey.ui.content.views;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import taskey.constants.UiConstants;
import taskey.messenger.Task;
import taskey.ui.utility.UiGridHelper;
import taskey.ui.utility.UiImageManager;
import taskey.ui.utility.UiImageManager.ImageID;
import taskey.ui.utility.UiTextBuilder;

/**
 * @@author A0125419H
 * This class is used to separate out the displaying of the task list
 * as DefaultFormatter, ActionFormatter both use it
 * 
 * @author junwei
 * 
 */

public class UiTaskView {
	private static final int stackPanePadding = 4;
	
	private UiPagination taskView;
	private int entriesPerPage;
	private UiGridHelper gridHelper;

	public UiTaskView(int _entriesPerPage) {
		entriesPerPage = _entriesPerPage;
		gridHelper = new UiGridHelper(UiConstants.STYLE_DEFAULT_BOX);
		taskView = new UiPagination(UiConstants.STYLE_HIGHLIGHT_BOX);
	}
	
	public UiPagination getView() {
		return taskView;
	}
	
	public void createPaginationGrids(ScrollPane mainPane, ArrayList<Task> myTaskList, int totalPages) {
		int entryNo = 0;
		ArrayList<Pair<Integer,Integer>> overlappingPairs = getIndexesOfOverlappingEvents(myTaskList);
		
		for ( int i = 0; i < totalPages; i ++ ) {
			GridPane newGrid = gridHelper.setUpGrid(UiConstants.GRID_SETTINGS_DEFAULT);
			ArrayList<StackPane> pageEntries = new ArrayList<StackPane>();
			for ( int j = 0; j < entriesPerPage; j ++ ) {
				if ( entryNo >= myTaskList.size() ) {
					break;
				}
				StackPane entryPane = gridHelper.createStyledCell(1, j, UiConstants.STYLE_DEFAULT_BOX, newGrid);
				// another grid within the pane for formatting while keeping a common styling pane
				GridPane paneGrid = gridHelper.setUpGrid(UiConstants.GRID_SETTINGS_ENTRY_PANE); 
				entryPane.getChildren().add(paneGrid);
				
				Task theTask = myTaskList.get(entryNo);
				addTaskID(theTask, entryNo, j, newGrid); // add to main grid
				addTaskDescription(theTask,paneGrid);
				addImage(theTask,paneGrid);
				checkOverlaps(overlappingPairs, entryNo, paneGrid); // Mark overlapping tasks
				
				entryNo++;	
				pageEntries.add(entryPane);
			}
			taskView.addGridToPagination(newGrid,pageEntries);
		}
		taskView.setScrollPane(mainPane);
		taskView.initializeDisplay(totalPages); // update UI and bind call back
		
	}
	
	private void addTaskID(Task theTask, int id, int row, GridPane theGrid) {
		assert(theTask != null);
		UiTextBuilder myBuilder = new UiTextBuilder();
		myBuilder.addMarker(0, UiConstants.STYLE_TEXT_DEFAULT);
		String line = "" + (id + 1);
		Color theColor = null;
		switch ( theTask.getPriority()) {
			case 3: theColor = Color.RED;
					break;
			case 2: theColor = Color.web("#e87301",1.0);
					break;
			default:
					theColor = Color.GREEN;
		}
		gridHelper.createStyledCell(0, row, "", theGrid);
		gridHelper.createScaledRectInCell(0, row, theColor, theGrid);
		gridHelper.addTextFlowToCell(0, row, myBuilder.build(line),TextAlignment.CENTER, theGrid);	
	}
		
	private void addTaskDescription(Task theTask, GridPane newGrid) {
		assert(theTask != null);
		assert(theTask.getTaskType() != null);
		
		UiTextBuilder myBuilder = new UiTextBuilder();
		myBuilder.addMarkers(UiConstants.STYLE_TEXT_DEFAULT);
		String line = "";
		line += "Name: "; 
		line += theTask.getTaskName() + "\n";
		switch ( theTask.getTaskType() ) {
			case "EVENT": 
				String [] timings = theTask.getEventTime();
				line += "from ";
				line += timings[0] + " to " + timings[1];
				break;
	 		case "DEADLINE":
				line += "by ";	
				line += "" + theTask.getDeadline();
				break;
			default:
				break;
		}
		line += "\n";
		if ( theTask.getTaskTags() != null ) {
			line += "Tags: ";
			ArrayList<String> tags = theTask.getTaskTags();
			for ( int i = 0; i < tags.size()-1 ; i++ ) {
				line += "#" + tags.get(i) + " ";
			}
			line += "#" + tags.get(tags.size()-1);
		} 
		StackPane pane = gridHelper.createStyledCell(0, 0, "", newGrid);
		pane.setPadding(new Insets(stackPanePadding));
		gridHelper.addTextFlowToCell(0, 0, myBuilder.build(line),TextAlignment.LEFT, newGrid);
	}
	
	private void addImage(Task theTask, GridPane newGrid) { 
		assert(theTask.getTaskType() != null);
		ImageID imgID;
		switch ( theTask.getTaskType() ) {
			case "EVENT":
				imgID = ImageID.EVENT;
				break;
			case "DEADLINE":
				imgID = ImageID.DEADLINE;
				break;
			default:
				imgID = ImageID.FLOATING;
				break;
		}
		gridHelper.createStyledCell(1, 0, "", newGrid);
		gridHelper.createImageInCell(1,0,UiImageManager.getInstance().getImage(imgID),30,30,newGrid);
	}
	
	private void addOverlapIcon(GridPane newGrid ) {
		ImageView img = gridHelper.createImageInCell(1,0,
				UiImageManager.getInstance().getImage(ImageID.URGENT_MARK),15,15,newGrid);
		StackPane.setAlignment(img, Pos.TOP_RIGHT);
	}
	
	private void checkOverlaps(ArrayList<Pair<Integer, Integer>> overlappingPairs, int entryNo, GridPane paneGrid) {
		for ( int k = 0; k < overlappingPairs.size(); k ++ ) {
			if ( overlappingPairs.get(k).getKey() == entryNo 
				 || overlappingPairs.get(k).getValue() == entryNo) {
				addOverlapIcon(paneGrid);
				break;
			}
		}
	}

	/**
	 * This method provides an efficient method to check overlap of 2 ranges
	 * http://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
	 * Assumes start date is <= end date for both events
	 * @param firstEvent
	 * @param secondEvent
	 * @return
	 */
	private boolean isOverlapping ( Task firstEvent, Task secondEvent) {
		return (firstEvent.getStartDateEpoch() <= secondEvent.getEndDateEpoch())  &&  
				(firstEvent.getEndDateEpoch() >= secondEvent.getStartDateEpoch());
	}
	
	private ArrayList<Pair<Integer,Integer>> getIndexesOfOverlappingEvents(ArrayList<Task> taskList) {
		ArrayList<Pair<Integer,Integer>> indexPairs = new ArrayList<Pair<Integer,Integer>>();
		ArrayList<Pair<Integer,Task>> events = new ArrayList<Pair<Integer,Task>>();
		for ( int i = 0; i < taskList.size(); i ++ ) {
			if ( taskList.get(i).getTaskType().equals("EVENT")) {
				events.add(new Pair<Integer,Task>(i,taskList.get(i)));  // original index in task list
			}
		}
		if ( events.size() > 0 ) {
			for ( int i = 0; i < events.size(); i ++ ) {
				for ( int j = i+1; j < events.size(); j ++) {
					if ( isOverlapping(events.get(i).getValue(), events.get(j).getValue())) {
						indexPairs.add(new Pair<Integer,Integer>(events.get(i).getKey(),events.get(j).getKey()));
					}
				}
			}
		}
		return indexPairs;
	}
	
	public void clear() {
		taskView.clear();
	}
}
