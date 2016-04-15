package at.foorada;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.regex.Pattern;

public class Controller {

    @FXML private Label startIdxLbl;
    @FXML private Label endIdxLbl;
    @FXML private Label appendingTextLbl;
    @FXML private Label appendingIdxLbl;
    @FXML private Label numberingPositionLbl;
    @FXML private Label numberingStartLbl;

    @FXML private TextField startIdxField;
    @FXML private TextField endIdxField;
    @FXML private TextField appendingIdxField;
    @FXML private TextField directoryString;
    @FXML private TextField appendingTextField;
    @FXML private TextField numberingNumberPositionTextField;
    @FXML private TextField numberingStartNumberTextField;

    @FXML private CheckBox removeTextChk;
    @FXML private CheckBox appendingTextChk;
    @FXML private CheckBox numberingChk;

    @FXML private Button directoryBtn;

    private int numberingCounter = 0,appendingTextIdx =0, numberingIdx=0;
    private String textAppendix = "";

    @FXML private TableView<RenamingFile> renamingTable;
    private final ObservableList<RenamingFile> data = FXCollections.observableArrayList();

    public void checkOptions() {
        boolean removeActive = removeTextChk.isSelected();
        removeTextChk.getParent().setStyle("-fx-border-color: "+(removeActive ? "black" : "lightGray")+"; -fx-border-radius:5px");
        startIdxLbl.setDisable(!removeActive);
        startIdxField.setDisable(!removeActive);
        endIdxLbl.setDisable(!removeActive);
        endIdxField.setDisable(!removeActive);

        boolean appendingActive = appendingTextChk.isSelected();
        appendingTextChk.getParent().setStyle("-fx-border-color: "+(appendingActive ? "black" : "lightGray")+"; -fx-border-radius:5px");
        appendingTextLbl.setDisable(!appendingActive);
        appendingTextField.setDisable(!appendingActive);
        appendingIdxLbl.setDisable(!appendingActive);
        appendingIdxField.setDisable(!appendingActive);

        boolean numberingActive = numberingChk.isSelected();
        numberingChk.getParent().setStyle("-fx-border-color: "+(numberingActive ? "black" : "lightGray")+"; -fx-border-radius:5px");
        numberingStartLbl.setDisable(!numberingActive);
        numberingStartNumberTextField.setDisable(!numberingActive);
        numberingPositionLbl.setDisable(!numberingActive);
        numberingNumberPositionTextField.setDisable(!numberingActive);
    }

    private void readValuesFromGUI() {

        if(appendingTextChk.isSelected()) {
            try {
                appendingTextIdx = Integer.parseInt(appendingIdxField.getText());
            } catch (NumberFormatException nfe) {
                appendingTextIdx = 0;
            }
            textAppendix = appendingTextField.getText();
        }
        if(numberingChk.isSelected()) {
            try {
                numberingIdx = Integer.parseInt(numberingNumberPositionTextField.getText());
            } catch (NumberFormatException nfe) {
                numberingIdx = 0;
            }
        }
    }

    private void resetNumberingCnt() {
        try {
            this.numberingCounter = Integer.parseInt(numberingStartNumberTextField.getText());
        } catch(NumberFormatException nfe) {
            this.numberingCounter = 0;
        }
    }

    private String removeText(String oldText) {
        int endIdx = Integer.parseInt(endIdxField.getText());

        String fileNameWithoutExtention = oldText.substring(0, oldText.lastIndexOf("."));

        if(endIdx >= fileNameWithoutExtention.length())
            endIdx = fileNameWithoutExtention.length();

        if(endIdx >= oldText.length())
            endIdx = oldText.length();

        int startIdx = Integer.parseInt(startIdxField.getText());

        if(startIdx > endIdx)
            startIdx = endIdx;

        String pre = oldText.substring(0, startIdx);
        String post = oldText.substring(endIdx, oldText.length());
        return pre + post;
    }

    private String appendText(String oldText, String appendix, int idx) {

        String fileNameWithoutExtension = oldText.substring(0, oldText.lastIndexOf("."));

        if(idx >= fileNameWithoutExtension.length())
            idx = fileNameWithoutExtension.length();

        if(idx >= oldText.length())
            idx = oldText.length();

        String begin = oldText.substring(0,idx);
        String end = oldText.substring(idx);

        return begin + appendix + end;
    }

    private String addNumbering(String oldText, int idx) {

        return appendText(oldText, String.format("%05d", this.numberingCounter++), idx);

    }

    protected void loadFileList(File folder) {

        try {
            directoryString.setText(folder.getPath());
            File[] fullList = folder.listFiles();
            data.clear();
            for (File f : fullList) {
                if (f.isFile()) {
                    this.data.add(new RenamingFile(f, renamingTable));
                }
            }
        } catch(NullPointerException e) {}
    }

    private void updateDisplayedFileList() {
        checkOptions();
        resetNumberingCnt();

        readValuesFromGUI();

        StringBuilder bfr = new StringBuilder();

        try {
            boolean isFirst = true;

            for(RenamingFile f:data) {

                if(!isFirst) {
                    bfr.append("\n");
                }
                else {
                    isFirst = false;
                }
                String fileName = f.getName();
                String fileNameWithoutExtention = fileName.substring(0, fileName.lastIndexOf("."));

                if(removeTextChk.isSelected()) {
                    fileName = this.removeText(fileName);
                }
                if(appendingTextChk.isSelected())
                    fileName = this.appendText(fileName, this.textAppendix, this.appendingTextIdx);
                if(numberingChk.isSelected())
                    fileName = this.addNumbering(fileName, numberingIdx);

                f.setNewName(fileName);

            }

            renamingTable.refresh();

//            if(isFirst) {
//                bfr.append("Keine Daten im ausgewählten Ordner vorhanden.");
//            }
        } catch (NullPointerException npe) {
            bfr.append("Angegebener ordner nicht korrekt.");
        }
    }

    @FXML protected void updateScreenOnAction(ActionEvent event) {
        this.updateDisplayedFileList();
    }

    @FXML protected void preCheckStartIdx(KeyEvent event) {

        String input = event.getCharacter();
        if (Pattern.matches("[^0-9]", input))
            event.consume();
    }

    @FXML protected void checkStartIdx(KeyEvent event) {
        try {
            int idx = Integer.parseInt(startIdxField.getText());
            if(idx < 0) {
                startIdxField.setText("0");
            }
        } catch(NumberFormatException nfe) {
            startIdxField.setText("0");
        }
        updateDisplayedFileList();
    }

    @FXML protected void preCheckEndIdx(KeyEvent event) {

        String input = event.getCharacter();
        if (Pattern.matches("[^0-9]", input))
            event.consume();
    }

    @FXML protected void checkEndIdx(KeyEvent event) {
        try {
            int idx = Integer.parseInt(endIdxField.getText());
            if(idx < 0) {
                endIdxField.setText("0");
            }
        } catch(NumberFormatException nfe) {
            endIdxField.setText("0");
        }
        updateDisplayedFileList();
    }

    @FXML protected void preCheckAppendingIdx(KeyEvent event) {

        String input = event.getCharacter();
        if (Pattern.matches("[^0-9]", input))
            event.consume();
    }

    @FXML protected void checkAppendingIdx(KeyEvent event) {

        try {
            int idx = Integer.parseInt(appendingIdxField.getText());
            if(idx < 0) {
                appendingIdxField.setText("0");
            }
        } catch(NumberFormatException nfe) {
            appendingIdxField.setText("0");
        }

        updateDisplayedFileList();
    }

    @FXML protected void preCheckAppendingText(KeyEvent event) {

        String input = event.getCharacter();
        if (Pattern.matches("[^a-zA-Z0-9_]", input))
            event.consume();
    }

    @FXML protected void checkAppendingText(KeyEvent event) {

        updateDisplayedFileList();
    }

    @FXML protected void preCheckNumberingStartNumber(KeyEvent event) {
        String input = event.getCharacter();
        if (Pattern.matches("[^0-9]", input))
            event.consume();
    }

    @FXML protected void checkNumberingStartNumber(KeyEvent event) {
        try {
            int idx = Integer.parseInt(numberingStartNumberTextField.getText());
            if(idx < 0) {
                numberingStartNumberTextField.setText("0");
            }
        } catch(NumberFormatException nfe) {
            numberingStartNumberTextField.setText("0");
        }

        updateDisplayedFileList();
    }

    @FXML protected void preCheckNumberPosition(KeyEvent event) {
        String input = event.getCharacter();
        if (Pattern.matches("[^0-9]", input))
            event.consume();
    }

    @FXML protected void checkNumberPosition(KeyEvent event) {
        try {
            int idx = Integer.parseInt(numberingNumberPositionTextField.getText());
            if(idx < 0) {
                numberingNumberPositionTextField.setText("0");
            }
        } catch(NumberFormatException nfe) {
            numberingNumberPositionTextField.setText("0");
        }

        updateDisplayedFileList();
    }

    @FXML protected void directoryFieldAction(ActionEvent event) {

        File enteredFolder = new File(directoryString.getText());

        this.loadFileList(enteredFolder);
        this.updateDisplayedFileList();
    }

    @FXML protected void handleDirectoryChooser(ActionEvent event) {
        DirectoryChooser dChooser = new DirectoryChooser();

        try {
            File oldFolder = new File(directoryString.getText());
            if(oldFolder.isDirectory())
                dChooser.setInitialDirectory(oldFolder);
        } catch(NullPointerException npe) {}

        File selectedFolder = dChooser.showDialog(directoryBtn.getScene().getWindow());

        this.loadFileList(selectedFolder);
        this.updateDisplayedFileList();
    }

    @FXML protected void renameFilesBtn(ActionEvent event) {

        readValuesFromGUI();
        resetNumberingCnt();

        doRenaming();

        Alert alertException = new Alert(Alert.AlertType.INFORMATION);
        alertException.setTitle("Fertig");
        alertException.setHeaderText("Umbenennungen abgeschlossen.");
        alertException.showAndWait();

//        this.loadFileList(new File(directoryString.getText()));
        this.updateDisplayedFileList();
    }

    @FXML protected void keyPressedRenamingTableView(KeyEvent event) {
        if(event.getCode() == KeyCode.SPACE) {

            ObservableList<RenamingFile> list = renamingTable.getSelectionModel().getSelectedItems();

            for(RenamingFile f:list) {
                f.setChecked(!f.getChecked());
            }

            event.consume();
        }
    }

    private void doRenaming() {

        for(RenamingFile f:data) {
            try {
                if(f.isChecked()) {
                    File newFile = new File(f.getParent(),f.getNewName());

                    if(f.renameTo(newFile)) {
                        RenamingFile rfile = new RenamingFile(newFile, renamingTable);
                        rfile.setChecked(true);
                        try {
                            data.set(data.indexOf(f), rfile);
                        } catch(ClassCastException | NullPointerException exp) { }
                    } else {
                            Alert alertRenameFailed = new Alert(Alert.AlertType.ERROR);
                            alertRenameFailed.setTitle("Umbennenung nicht möglich");
                            alertRenameFailed.setHeaderText("Ein file konnte nicht umbenannt werden.");
                            alertRenameFailed.setContentText(f.getPath() +"\n->\n"+newFile.getPath());
                            alertRenameFailed.showAndWait();
                    }
                }

            } catch(Exception e) {

                Alert alertException = new Alert(Alert.AlertType.ERROR);
                alertException.setTitle("Umbennenung nicht möglich");
                alertException.setHeaderText("Ein file konnte nicht umbenannt werden.");
                alertException.setContentText(f.getPath());
                alertException.showAndWait();
            }
        }

    }

    public void prepareTable() {
        renamingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        renamingTable.setItems(data);
    }

    public static class RenamingFile extends File {
        private SimpleBooleanProperty checked;
        private final SimpleStringProperty newName;
        private final TableView<RenamingFile> renTable;

        public RenamingFile(File f, TableView<RenamingFile> renamingTable) {
            this(f.getPath(), renamingTable);
        }

        private RenamingFile(String filename, TableView<RenamingFile> renamingTable) {
            super(filename);
            this.renTable = renamingTable;
            this.checked = new SimpleBooleanProperty(false);
            this.checked.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    renTable.refresh();
                    renTable.requestFocus();
                }
            });
            this.newName = new SimpleStringProperty(this.getName());
        }

        public String getCurrentName() {
            return this.getName();
        }

        public String getNewName() {
            if(isChecked())
                return newName.get();
            else
                return getCurrentName();
        }

        public void setNewName(String newFileName) {
            this.newName.set(newFileName);
        }


        public SimpleBooleanProperty checkedProperty() {
            return this.checked;
        }
        public boolean getChecked() {
            return this.checked.get();
        }

        public void setChecked(boolean checked) {
            this.checked.set(checked);
        }

        public boolean isChecked(){
            return this.getChecked();
        }
    }
}