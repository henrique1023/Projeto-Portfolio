package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErrorId;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;

	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Label labelErrorBirthDate;

	@FXML
	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setService(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	// esse metodo inscreve a outra interface para ouvir essa interface
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.saveOrUptade(entity);
			// quando é confirmado o salvar ele emite um alerta para atualizar a pagina
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorsMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error savings Object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	// esse metodo que gera o novo alerta para o sistema
	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	// esse metodo que atribui os atributos passados no classe Seller
	private Seller getFormData() {
		Seller obj = new Seller();

		ValidationException exception = new ValidationException("Validation Errors");

		obj.setId(Utils.tryParseToInt(txtId.getText()));

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Fields can't be emply");
		}

		obj.setName(txtName.getText());

		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("email", "Fields can't be emply");
		}

		obj.setEmail(txtEmail.getText());
		
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "Fields can't be emply");
		}

		obj.setBasaSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));
	
		if (dpBirthDate.getValue() == null) {
			exception.addError("birthDate", "Fields can't be emply");
		}else {
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
		
			obj.setBirthDate(Date.from(instant));
		}
		
		obj.setDepartment(comboBoxDepartment.getValue());
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	public void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();
	}

	// esse metodo insere os valores que estão na entidade no formulario
	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null!!");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		txtBaseSalary.setText(String.format("%.2f", entity.getBasaSalary()));
		// para deixar no formato aceito pela maquina, o date precisa desse tratamento
		if (entity.getBirthDate() != null) {
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		if(entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("Department Service was null");
		}
		List<Department> list = departmentService.findAll();
		
		// esse função pega os itens da lista e passa para obsList
		obsList = FXCollections.observableArrayList(list);

		// comboBox só aceita itens que estejam no ObservableList
		comboBoxDepartment.setItems(obsList);
	}

	// esse metodo verifica se tem o erro e manda ele para o label
	public void setErrorsMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		labelErrorName.setText(fields.contains("name") ? errors.get("name") : "");
		labelErrorEmail.setText(fields.contains("email") ? errors.get("email") : "");
		labelErrorBaseSalary.setText(fields.contains("baseSalary") ? errors.get("baseSalary") : "");
		if (fields.contains("birthDate")) {
			labelErrorBirthDate.setText(errors.get("birthDate"));
		}else {
			labelErrorBirthDate.setText("");
		}
		labelErrorBirthDate.setText(fields.contains("birthDate") ? errors.get("birthDate") : "");
		
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
