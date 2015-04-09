package com.creatingskies.game.config.event;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameConverter;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyConverter;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.model.event.GameEventDao;

public class GameEventPropertiesViewController extends PropertiesViewController{

	@FXML private ComboBox<Company> companyComboBox;
	@FXML private ComboBox<Game> gameComboBox;
	@FXML private DatePicker eventDatePicker;
	@FXML private TextField hourTextField;
	@FXML private TextField minuteTextField;
	@FXML private ChoiceBox<String> periodChoiceBox;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	@FXML private Button backToListButton;
	
	public void init(){
		super.init();
		
		companyComboBox.getItems().clear();
		companyComboBox.getItems().add(null);
		companyComboBox.getItems().addAll(new CompanyDAO().findAllCompanies());
		companyComboBox.setConverter(new CompanyConverter());
		companyComboBox.setCellFactory(new Callback<ListView<Company>,ListCell<Company>>(){
            @Override
            public ListCell<Company> call(ListView<Company> p) {
                 
                final ListCell<Company> cell = new ListCell<Company>(){
 
                    @Override
                    protected void updateItem(Company t, boolean bln) {
                        super.updateItem(t, bln);
                         
                        if(t != null){
                            setText(t.getName());
                        }else{
                            setText(null);
                        }
                    }
  
                };
                return cell;
            }
        });
		
		gameComboBox.getItems().clear();
		gameComboBox.getItems().add(null);
		gameComboBox.getItems().addAll(new GameDao().findAllGames());
		gameComboBox.setConverter(new GameConverter());
		gameComboBox.setCellFactory(new Callback<ListView<Game>,ListCell<Game>>(){
            @Override
            public ListCell<Game> call(ListView<Game> p) {
                 
                final ListCell<Game> cell = new ListCell<Game>(){
 
                    @Override
                    protected void updateItem(Game t, boolean bln) {
                        super.updateItem(t, bln);
                         
                        if(t != null){
                            setText(t.getTitle());
                        }else{
                            setText(null);
                        }
                    }
  
                };
                return cell;
            }
        });
		
		hourTextField.addEventFilter(KeyEvent.KEY_TYPED, Util.createIntegerOnlyKeyEvent());
		hourTextField.textProperty().addListener(Util.createIntegerOnlyChangeListener(hourTextField,12));
		minuteTextField.addEventFilter(KeyEvent.KEY_TYPED, Util.createIntegerOnlyKeyEvent());
		minuteTextField.textProperty().addListener(Util.createIntegerOnlyChangeListener(minuteTextField,59));
		
		periodChoiceBox.getItems().clear();
		periodChoiceBox.getItems().add("AM");
		periodChoiceBox.getItems().add("PM");
		
		companyComboBox.getSelectionModel().select(getGameEvent().getCompany());;
		gameComboBox.getSelectionModel().select(getGameEvent().getGame());
		
		
		eventDatePicker.setValue(Util.toLocalDate(getGameEvent().getEventDate()));
		hourTextField.setText(String.valueOf(Util.getHourFromDate(getGameEvent().getEventDate())));
		minuteTextField.setText(String.valueOf(Util.getMinuteFromDate(getGameEvent().getEventDate())));
		periodChoiceBox.getSelectionModel().select(Util.getAMPMFromDate(getGameEvent().getEventDate()));
		
		disableFields();
	}
	
	private void disableFields(){
		if(getCurrentAction() == Action.VIEW){
			companyComboBox.setDisable(true);
			gameComboBox.setDisable(true);
			eventDatePicker.setEditable(false);
			hourTextField.setEditable(false);
			minuteTextField.setEditable(false);
			periodChoiceBox.setDisable(true);
			
			saveButton.setVisible(false);
			cancelButton.setVisible(false);
			backToListButton.setVisible(true);
		}else{
			companyComboBox.setDisable(false);
			gameComboBox.setDisable(false);
			eventDatePicker.setEditable(false);
			hourTextField.setEditable(true);
			minuteTextField.setEditable(true);
			periodChoiceBox.setDisable(false);
			
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
			backToListButton.setVisible(false);
		}
	}
	
	@FXML
	private void save(){
		if(isValid()){
			getGameEvent().setCompany(companyComboBox.getValue());
			getGameEvent().setGame(gameComboBox.getValue());
			getGameEvent().setEventDate(getEventDate());
			new GameEventDao().saveOrUpdate(getGameEvent());
			close();
			new GameEventTableViewController().show();
		}
	}
	
	@FXML
	private void cancel(){
		backToList();
	}
	
	@FXML
	private void backToList(){
		close();
		new GameEventTableViewController().show();
	}
	
	private boolean isValid(){
		if(companyComboBox.getSelectionModel().getSelectedItem() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Company is required.").showAndWait();
			return false;
		}
		if(gameComboBox.getSelectionModel().getSelectedItem() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Game is required.").showAndWait();
			return false;
		}
		if(eventDatePicker.getValue() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Date is required.").showAndWait();
			return false;
		}
		if(Util.isBlank(hourTextField.getText()) && Util.isZero(hourTextField.getText())){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Event hour is required.").showAndWait();
			return false;
		}
		if(Util.isBlank(minuteTextField.getText()) && Util.isZero(minuteTextField.getText())){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Event minute is required.").showAndWait();
			return false;
		}
		GameEvent event = new GameEventDao().findEventByDate(getEventDate());
		if(event != null){
			new AlertDialog(AlertType.ERROR, "Ooops", null, "Event time is not available.").showAndWait();
			return false;
		}
		
		return true;
	}
	
	private Date getEventDate(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(Instant.from(eventDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()))));
		
		int hour = Integer.parseInt(hourTextField.getText());
		int minute = Integer.parseInt(minuteTextField.getText());
				
		cal.set(Calendar.HOUR, (hour == 12) ? 0 : hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.AM_PM, (periodChoiceBox.getSelectionModel().equals("AM")) ? 0 : 1);
		
		return cal.getTime();
	}
	
	public void show(Action action,GameEvent gameEvent){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameEventProperties.fxml"));
            AnchorPane event = (AnchorPane) loader.load();
            
            GameEventPropertiesViewController controller = (GameEventPropertiesViewController) loader.getController();
            controller.setCurrentAction(action);
            controller.setCurrentRecord(gameEvent);
            controller.init();
            
            MainLayout.getRootLayout().setCenter(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	protected String getViewTitle() {
		return "Event Details";
	}
	
	public GameEvent getGameEvent(){
		return (GameEvent) getCurrentRecord();
	}
	
	public void setGameEvent(GameEvent gameEvent){
		setCurrentRecord(gameEvent);
	}
	
}
