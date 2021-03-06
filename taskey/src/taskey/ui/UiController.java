package taskey.ui;

import java.util.ArrayList;
import java.util.logging.Level;

import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import taskey.constants.Triplet;
import taskey.constants.UiConstants;
import taskey.constants.UiConstants.ActionMode;
import taskey.constants.UiConstants.ContentBox;
import taskey.logger.TaskeyLog;
import taskey.logger.TaskeyLog.LogSystems;
import taskey.logic.Logic;
import taskey.logic.LogicFeedback;
import taskey.logic.LogicMemory;
import taskey.messenger.ProcessedObject;
import taskey.messenger.TagCategory;
import taskey.messenger.Task;
import taskey.ui.content.UiContentManager;
import taskey.ui.utility.UiAnimationManager;
import taskey.ui.utility.UiImageManager;
import taskey.ui.utility.UiImageManager.ImageID;
import taskey.ui.utility.UiPopupManager;

/**
 * @@author A0125419H
 * 
 * This class is the main class that handles all of the main window Ui nodes
 * UiController is the main interface between Ui and Logic
 * One sub interface would be UiUpdateService inside UiController
 * 
 * This class is responsible for handling all user keyboard / mouse inputs within the
 * main window, and delegates the actions to be handled into other classes if needed
 * 
 * @author JunWei
 * 
 */

public class UiController {

	@FXML
	private TabPane myTabs;
	@FXML
	private TextField input;
	@FXML
	private Label dateLabel;
	@FXML
	private StackPane dragBar;
	@FXML
	private ScrollPane categoryPane;
	@FXML
	private ImageView crossButton;
	@FXML
	private ImageView minusButton;
	@FXML
	private Label expiredIcon;
	@FXML
	private Label notification;
	
	private int mouseX, mouseY;
	private Stage stage;
	private Logic logic;
	private UiUpdateService updateService;
	private UiDropDown myDropDown;
	private UiContentManager myContentManager;
	private int currentTab;
	private ContentBox currentContent;
	private ArrayList<String> inputHistory;
	private int historyIterator;
	private Timeline shakeNotification; // animation

	//----- Used by UiTrayModule ------
	public Stage getStage() {
		return stage;
	}
	
	public void setUpUpdateService(UiAlertsController alertController) {
		updateService = new UiUpdateService(dateLabel,logic,alertController);
		updateService.start();
	}
	
	public void updateAlerts() {
		updateService.pollFromLogic();
	}
	//---------------------------------
	
	/**
	 * Performs the main setups for the controller
	 * including nodes and logic
	 *
	 * @param primaryStage the primary stage
	 * @param root the root
	 */
	public void setUpController(Stage primaryStage, Parent root) {
		assert(primaryStage != null);
		assert(root != null);
		TaskeyLog.getInstance().log(LogSystems.UI, "Setting up Main Controller...", Level.ALL);
		
		stage = primaryStage; // set up reference
		myDropDown = new UiDropDown();
		setUpContentBoxes();
		setUpTabDisplay();		
		setUpButtonStyles();
		setUpInput();
		registerEventHandlersToNodes(root);	
		setUpLogic();	

		TaskeyLog.getInstance().log(LogSystems.UI, "Main Controller has been set up...", Level.ALL);
	}
	
	/**
	 * Sets up nodes which need bounds.
	 * nodes or classes that need layout bounds are initialized here
	 * (Note that bounds are updated only when the node is shown)
	 */
	public void setUpNodesWhichNeedBounds() {
		assert(myDropDown != null);
		myDropDown.createMenu(stage, input);
	}

	private void setUpContentBoxes() {
		assert(myTabs != null);
		myContentManager = new UiContentManager();
		for (int i = 0; i < myTabs.getTabs().size(); i++) {
			AnchorPane tabContent = (AnchorPane) myTabs.getTabs().get(i).getContent();
			ScrollPane content = (ScrollPane) tabContent.getChildren().get(0);
			myContentManager.setUpContentBox(content, ContentBox.fromInteger(i)); // add scrollpanes
		}
		myContentManager.setUpContentBox(categoryPane,ContentBox.CATEGORY);
	}

	private void setUpTabDisplay() {
		currentTab = 0;
		input.requestFocus();
		displayTabContents(ContentBox.THIS_WEEK);
	}
	
	private void setUpButtonStyles() {
		crossButton.setImage(UiImageManager.getInstance().getImage(ImageID.CROSS_DEFAULT)); 
		minusButton.setImage(UiImageManager.getInstance().getImage(ImageID.MINUS_DEFAULT)); 
	}
	
	/**
	 * Sets up variables related to input, including feedbacks
	 */
	private void setUpInput() {
		input.getStyleClass().add(UiConstants.STYLE_TEXT_ALL);
		input.getStyleClass().add(UiConstants.STYLE_INPUT_NORMAL);		
		inputHistory = new ArrayList<String>();
		historyIterator = 0;
		shakeNotification = UiAnimationManager.getInstance().createShakeTransition(notification, 
																	   UiConstants.DEFAULT_SHAKE_DISTANCE, 
																	   UiConstants.DEFAULT_SHAKE_INTERVAL, 
																	   UiConstants.DEFAULT_ANIM_DURATION);
	}
	
	private void registerEventHandlersToNodes(Parent root) {
		registerInputEventHandler();
		registerRootEventHandler(root);
		registerDragHandler();
		registerButtonHandlers();
	}
	
	private void setUpLogic() {
		logic = new Logic();
		updateAllContents(logic.getTagCategoryList(),logic.getAllTaskLists());
	}
	
	private void displayTabContents(ContentBox toContent) {
		SingleSelectionModel<Tab> selectionModel = myTabs.getSelectionModel();
		selectionModel.select(toContent.getValue());
	}
	
	private ContentBox getCurrentContent() {
		currentContent = ContentBox.fromInteger(myTabs.getSelectionModel().getSelectedIndex());
		return currentContent;
	}
	
	/**
	 * Sets scene style sheets, input is assumed to be valid before calling
	 * this method, if input is invalid, prints an exception message
	 *
	 * @param styleSheets - style sheets to use for the display as an Array List
	 */
	public void setStyleSheets(ArrayList<String> styleSheets) {
		assert(styleSheets != null);
		ObservableList<String> myStyleSheets = stage.getScene().getStylesheets();
		myStyleSheets.clear();
		try {
			for (int i = 0; i < styleSheets.size(); i++) { // load all style sheets into list
				myStyleSheets.add(getClass().getResource(UiConstants.UI_CSS_PATH_OFFSET 
														 + styleSheets.get(i)).toExternalForm());
			}
		} catch (Exception excep) {
			System.out.println(excep + UiConstants.STYLE_SHEETS_LOAD_FAIL);
		}
	}
	
	private void handleFeedback( LogicFeedback feedback ) {
		assert(feedback != null);
		Exception statusCode = feedback.getException();
		if ( statusCode != null ) {
			notification.setText(statusCode.getMessage());	
			shakeNotification.playFromStart();
		}
		
		ArrayList<ArrayList<Task>> allLists = feedback.getTaskLists();	
		ProcessedObject processed = feedback.getPo();
		String command = processed.getCommand();
		switch (command) {		 // change display based on which command was processed
			case "ADD_DEADLINE": 
			case "ADD_EVENT":
			case "ADD_FLOATING":
				displayTabContents(ContentBox.PENDING);
				break;
			case "VIEW_BASIC":
				if ( processed.getViewType().get(0).equals("help")) {
					displayTabContents(ContentBox.ACTION);
					myContentManager.setActionMode(UiConstants.ActionMode.HELP);
					return; // don't need to update all
				}
			case "VIEW_TAGS":
			case "SEARCH":
				displayTabContents(ContentBox.ACTION);
				myContentManager.setActionMode(UiConstants.ActionMode.LIST);
				break;	
			case "ERROR":
				return;
			default:
				break;
		}
		// just update all displays, rather than splitting it into each switch case
		updateAllContents(logic.getTagCategoryList(),allLists); 
	}
	
	/**
	 * Create a header of fixed categories for the category list
	 * @param allLists - all the task lists
	 * @return categoryListHeader
	 */
	private ArrayList<Triplet<Color, String, Integer>> createCategoriesHeader(ArrayList<ArrayList<Task>> allLists) {
		ArrayList<Triplet<Color,String,Integer>> categoryListHeader = new ArrayList<Triplet<Color,String,Integer>>();
		ArrayList<Task> pendingList = allLists.get(LogicMemory.INDEX_PENDING);
		ArrayList<Task> expiredList = allLists.get(LogicMemory.INDEX_EXPIRED);
		
		int priorityNums[] = new int[3];
		for ( int i = 0; i < pendingList.size(); i++ ) {
			priorityNums[pendingList.get(i).getPriority()-1]++; // increase numbers for each priority
		}
		for ( int i = 0; i < expiredList.size(); i ++ ) {
			priorityNums[expiredList.get(i).getPriority()-1]++; // do the same for expired
		}
		
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.RED,"HIGH", priorityNums[2]));
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.web("#e87301",1.0),"MED", priorityNums[1]));
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.GREEN,"LOW", priorityNums[0]));
		
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.CADETBLUE,"General",
																 allLists.get(LogicMemory.INDEX_FLOATING).size()));
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.CADETBLUE,"Deadlines",
																 allLists.get(LogicMemory.INDEX_DEADLINE).size()));
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.CADETBLUE,"Events",
																 allLists.get(LogicMemory.INDEX_EVENT).size()));
		categoryListHeader.add(new Triplet<Color,String,Integer>(Color.CADETBLUE,"Archive",
																 allLists.get(LogicMemory.INDEX_COMPLETED).size()));
		
		return categoryListHeader;
	}
	
	private void updateAllContents(ArrayList<TagCategory> tagList, ArrayList<ArrayList<Task>> allLists) {
		ArrayList<Triplet<Color,String,Integer>> categoryList = createCategoriesHeader(allLists);
		// Add tags in addition to the default categories
		for ( int i = 0 ; i < tagList.size(); i++ ) {
			categoryList.add(new Triplet<Color,String,Integer>(Color.DIMGRAY,tagList.get(i).getTagName(), 
															   tagList.get(i).getNumTags()));
		}
		// update every box
		myContentManager.updateCategoryContentBox(categoryList);
		myContentManager.updateContentBox(allLists.get(LogicMemory.INDEX_THIS_WEEK), UiConstants.ContentBox.THIS_WEEK);
		myContentManager.updateContentBox(allLists.get(LogicMemory.INDEX_PENDING), UiConstants.ContentBox.PENDING);
		myContentManager.updateContentBox(allLists.get(LogicMemory.INDEX_EXPIRED), UiConstants.ContentBox.EXPIRED);	
		myContentManager.updateContentBox(allLists.get(LogicMemory.INDEX_ACTION), UiConstants.ContentBox.ACTION);	
		expiredIcon.setText(String.valueOf(allLists.get(LogicMemory.INDEX_EXPIRED).size()));
	}
	
	public void cleanUp() {
		updateService.restart();
		myContentManager.cleanUp();
		UiPopupManager.getInstance().cleanUp();
	}
	
	//----------------------------------- EVENT HANDLERS -----------------------------------------
	private void registerInputEventHandler() {
		assert(input != null);
		input.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {			
				input.getStyleClass().remove(UiConstants.STYLE_INPUT_ERROR); // remove any error styles
				input.getStyleClass().remove(UiConstants.STYLE_INPUT_CORRECT); // remove any correct styles
				
				if ( isInputChanged(event.getCode()) == true ) {
					processAutoComplete();
				}
				if (event.getCode() == KeyCode.ENTER) {	
					processEnter();
				}
			}
		});
	
		// to override default events such as shifting the caret / cursor position to the start and end
		input.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (event.getCode().isArrowKey()) {
					if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) { 
						// get previous / next input history
						setInputFromHistory(event.getCode());
						event.consume();
					}
				} else if (event.getCode() == KeyCode.TAB) {
					event.consume();
				}
			};
		});
	}

	private boolean isInputChanged(KeyCode code) {
		if ( code.isArrowKey() == false && 
			 code.isDigitKey() ||
			 code.isLetterKey() || 
			 code == KeyCode.BACK_SPACE ||
			 code == KeyCode.SPACE) {
			return true;
		}
		return false;
	}
	
	private void processAutoComplete() {	
		if ( input.getText().isEmpty()) {
			myDropDown.closeMenu();
			return;
		}
		ArrayList<String> suggestions = logic.autoCompleteLine(input.getText().trim(), getCurrentContent());		
		if ( suggestions == null ) {
			input.getStyleClass().add(UiConstants.STYLE_INPUT_ERROR); // invalid input
			myDropDown.closeMenu();
		} else {
			input.getStyleClass().add(UiConstants.STYLE_INPUT_CORRECT); 		
			if ( suggestions.size() == 0 ) { // no suggestions but input is valid
				myDropDown.closeMenu();
			} else {
				myDropDown.updateMenuItems(suggestions);
				myDropDown.updateMenu();
			}
		}
	}
	
	/**
	 * This method adds a selection from the drop down
	 * to the input Textfield
	 * @param selection
	 */
	private void addSelectionToInput(String selection) {
		String currentLine = input.getText().trim();
		if ( selection.contains(currentLine)) { 
			input.setText(selection + " ");
		} else {	
			// special case
			currentLine = currentLine.replace("[", "[ "); // add space for processing
			currentLine = currentLine.replace("[  ", "[ "); // bound it within 1 space
			currentLine = currentLine.replace("]", " ]");
			currentLine = currentLine.replace("  ]", " ]"); 
			
			String [] lineTokens = currentLine.split(" ");		
			// remove all tokens from input that appear in current selection
			for ( int i = lineTokens.length-1; i >= 0; i-- ) {			
				if ( selection.contains(lineTokens[i])) {		
					// replace last occurrence of token
					int index = currentLine.lastIndexOf(lineTokens[i]);
					currentLine =  currentLine.substring(0, index) + 
					currentLine.substring(index + lineTokens[i].length(),currentLine.length()); 
				} else {
					break;
				}
			}	
			currentLine = currentLine.trim();
			input.setText(currentLine + " " + selection + " ");
		}
		input.selectEnd();
		input.deselect();
	}
	
	private void processEnter() {
		String selection = myDropDown.getSelectedItem();
		if ( selection.isEmpty() == false ) { // add selected item into input text
			addSelectionToInput(selection);
			myDropDown.closeMenu();
		} else  {
			String line = input.getText();
			if ( line.isEmpty() == false ) { // we send to command to logic for processing						
				input.clear();	
				myDropDown.closeMenu();
				handleFeedback(logic.executeCommand(getCurrentContent(),line));
				
				inputHistory.add(line);
				if ( inputHistory.size() > UiConstants.MAX_INPUT_HISTORY ) {
					inputHistory.remove(0);
					inputHistory.trimToSize();
				}
				historyIterator = inputHistory.size(); // set to size instead of size()-1, for up key to work properly
			} else {
				myContentManager.processEnter(getCurrentContent());
			}
		}
	}
	
	/**
	 * This method sets the textfield input depending on up and down arrows,
	 * which mean previous and next respectively.
	 * 
	 * @param code - KeyCode up or down
	 */
	private void setInputFromHistory( KeyCode code ) {
		if (inputHistory.size() != 0) {
			String line;
			if (code == KeyCode.UP) {
				historyIterator = Math.max(historyIterator - 1, 0);
			} else if (code == KeyCode.DOWN) {
				historyIterator++; 
			}
			if ( historyIterator > inputHistory.size()-1) {
				historyIterator = inputHistory.size(); // out of bounds
				line = "";
			} else {
				line = inputHistory.get(historyIterator);
			}
			input.setText(line);
		} else if ( code == KeyCode.DOWN ){ // wipe on down
			input.setText("");
		}
		input.selectEnd();
		input.deselect();
	}

	/**
	 * This method is for key inputs anywhere in main window
	 *
	 * @param root - root object of scene
	 */
	private void registerRootEventHandler(Parent root) {
		root.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				handleKeyPressInRoot(event);
			}
		});
		
		root.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				handleKeyReleaseInRoot(event);
			}
		});
	}
	
	private void handleKeyPressInRoot(KeyEvent event) {
		input.requestFocus(); // give focus to textfield on any inputs
		
		if (myDropDown.isMenuShowing()) {
			if (event.getCode().isArrowKey()) {	
				if ( event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
					myDropDown.processArrowKey(event);
					event.consume(); // give input only to drop down
				} else {
					myDropDown.closeMenu();
				}
			}
		} else if (input.getText().isEmpty()) { // user not typing in command, do pagination
			if (event.getCode() == KeyCode.DELETE) {
				int id = myContentManager.processDelete(getCurrentContent());
				if (id != 0) {
					handleFeedback(logic.executeCommand(getCurrentContent(), "del " + id));
				}
			} else if ( event.getCode().isArrowKey()) {
				if  ( event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
					myContentManager.processArrowKey(event, getCurrentContent());
				}
			}
		}
		if ( event.getCode() == KeyCode.PAGE_UP || event.getCode() == KeyCode.PAGE_DOWN) {
			myContentManager.processPageUpAndDown(event, getCurrentContent());
		}
	}
	
	private void handleKeyReleaseInRoot(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			currentTab = myTabs.getSelectionModel().getSelectedIndex();
			currentTab = (currentTab + 1) % myTabs.getTabs().size();
			displayTabContents(ContentBox.fromInteger(currentTab));
			event.consume();
		} else if (event.getCode() == KeyCode.ESCAPE) {
			doSaveOnExit();
		} else if (event.isControlDown() && event.getCode() == KeyCode.W){ // minimize
			crossButton.setImage(UiImageManager.getInstance().getImage(ImageID.CROSS_DEFAULT));  
			stage.close();
		} else if (event.getCode() == KeyCode.F1) {
			myContentManager.setActionMode(ActionMode.HELP);
			displayTabContents(ContentBox.ACTION);
		} else if (event.getCode() == KeyCode.F2) {
			setStyleSheets(UiConstants.STYLE_UI_DEFAULT);
		} else if (event.getCode() == KeyCode.F3) {
			setStyleSheets(UiConstants.STYLE_UI_LIGHT);
		} else if ( event.getCode() == KeyCode.Z && event.isControlDown()) { // undo
			handleFeedback(logic.executeCommand(getCurrentContent(), "undo"));
		}
	}
	
	private void registerDragHandler() {
		assert(dragBar != null);
		dragBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
			    // record X,Y differences
				mouseX = (int) (stage.getX() - mouseEvent.getScreenX());
				mouseY = (int) (stage.getY() - mouseEvent.getScreenY());
		  }
		});
		dragBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			  @Override public void handle(MouseEvent mouseEvent) {
			  	stage.setX(mouseEvent.getScreenX() + mouseX);
			    stage.setY(mouseEvent.getScreenY() + mouseY);
			    myDropDown.closeMenu();
			  } 
		});
	}
	
	private void registerButtonHandlers() {
		assert(crossButton != null);
		assert(minusButton != null);	
		crossButton.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				crossButton.setImage(UiImageManager.getInstance().getImage(ImageID.CROSS_SELECT));
		  }
		});
		crossButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				// 1st level intersect
				if ( mouseEvent.getPickResult().getIntersectedNode() == crossButton) {
					doSaveOnExit();
				} else {
					crossButton.setImage(UiImageManager.getInstance().getImage(ImageID.CROSS_DEFAULT));  
				}
			}
		});
		minusButton.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				minusButton.setImage(UiImageManager.getInstance().getImage(ImageID.MINUS_SELECT));
		  }
		});
		minusButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				if ( mouseEvent.getPickResult().getIntersectedNode() == minusButton) {
					minusButton.setImage(UiImageManager.getInstance().getImage(ImageID.MINUS_DEFAULT)); 
					stage.close();
				} else {
					minusButton.setImage(UiImageManager.getInstance().getImage(ImageID.MINUS_DEFAULT)); 
				}
			}
		});
	}
	
	public void doSaveOnExit() {
		logic.executeCommand(getCurrentContent(), "save");
		System.exit(0);
	}
}