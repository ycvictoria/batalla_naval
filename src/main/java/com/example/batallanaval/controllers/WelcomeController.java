package com.example.batallanaval.controllers;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.PlayerData;
import com.example.batallanaval.persistence.SaveManager;
import com.example.batallanaval.views.GameView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;

/**
 * Controlador para la pantalla de bienvenida (menú principal).
 * Gestiona la carga de partidas, el inicio de nuevos juegos, la validación del nombre
 * del jugador y la muestra de instrucciones.
 */
public class WelcomeController {

    // FXML
    @FXML private StackPane rootStack;
    @FXML private ImageView backgroundView;
    @FXML private HBox newGameBox;
    @FXML private TextField nameField;

    // Botones
    @FXML private Button btnContinue;
    @FXML private Button btnNew;
    @FXML private Button btnExit;
    @FXML private Button buttonHelp;
    @FXML private Button btnAccept;

    /**
     * Inicializa el controlador después de que los elementos FXML han sido cargados.
     * Configura el fondo, verifica la existencia de partidas guardadas y establece
     * los manejadores de eventos para los botones y campos de texto.
     */
    @FXML
    public void initialize() {
        // para que la imageView se ajuste al tamaño del StackPane.
        if (rootStack != null && backgroundView != null) {
            backgroundView.fitWidthProperty().bind(rootStack.widthProperty());
            backgroundView.fitHeightProperty().bind(rootStack.heightProperty());
        }
        // Verifica si hay partida guardada, si no existe el archivo, deshabilitamos el botón de Continuar
        if (SaveManager.loadPlayerInfo() == null) {
            btnContinue.setDisable(true);
        }
        // Ocultar sección de nuevo juego al inicio
        newGameBox.setVisible(false);
        newGameBox.setManaged(false);
        // Listener para limpiar la clase CSS de error cuando el usuario comienza a escribir
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            nameField.getStyleClass().remove("error");
        });

        // Manejo de evento cuando se presiona nuevo juego → mostrar campo nombre
        btnNew.setOnAction(e -> {
            newGameBox.setVisible(true);
            newGameBox.setManaged(true);
            nameField.requestFocus();
        });

        btnAccept.setOnAction(e -> startNewGame());
        nameField.setOnAction(e -> startNewGame());
        btnContinue.setOnAction(e -> continueGame());
        buttonHelp.setOnAction(e -> showInstructionsDialog());
        btnExit.setOnAction(e -> {
            Stage stage = (Stage) btnExit.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Inicia un nuevo juego después de validar el nickname del jugador.
     * Crea tableros nuevos, coloca los barcos de la máquina aleatoriamente
     * y guarda el estado inicial del juego.
     */
    private void startNewGame() {
        String nickname = nameField.getText().trim();
        if (nickname.length() < 3) {
            // Poner borde rojo al campo de texto si hay error.
            if (!nameField.getStyleClass().contains("error")) {
                nameField.getStyleClass().add("error");
            }
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);

            javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/com/example/batallanaval/theme.css").toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");
            dialogPane.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

            try {
                ImageView icon = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/example/batallanaval/barco_icon.png")));
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.setGraphic(icon);
            } catch (Exception ignored) { }

            alert.setTitle("Nombre muy corto");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, ingresa un nombre de al menos 3 caracteres.");
            alert.show();
            return;
        }

        Board playerBoard = new Board();
        Board machineBoard = new Board();
        machineBoard.randomizeShips(); // Colocación aleatoria para la IA

        // Guardado inicial de los tableros y la información de la IA
        SaveManager.saveBoard(playerBoard, "player_board.ser");
        SaveManager.saveBoard(machineBoard, "machine_board.ser");
        SaveManager.savePlayerInfo(nickname, 0, true);

        PlayerData data = new PlayerData(nickname, 0, true);
        openGame(playerBoard, machineBoard, data);
    }

    /**
     * Carga el estado de la partida guardada desde los archivos de persistencia
     * y abre la vista del juego con el estado recuperado.
     */
    private void continueGame() {
        Board player = SaveManager.loadBoard("player_board.ser");
        Board machine = SaveManager.loadBoard("machine_board.ser");
        PlayerData data = SaveManager.loadPlayerInfo();
        // Verificación de integridad de los archivos guardados
        if (player == null || machine == null || data == null) {
            System.out.println("❌ No hay partida guardada.");
            return;
        }
        openGame(
                player,
                machine,
                data
        );
    }

    /**
     * Abre la ventana principal del juego (GameView) y carga el estado
     * del juego proporcionado. Cierra la ventana de bienvenida.
     * @param player El tablero lógico del jugador humano.
     * @param machine El tablero lógico de la máquina.
     * @param data Los datos del jugador (nickname, puntaje, fase actual).
     */
    private void openGame(Board player,
                          Board machine,
                       PlayerData data) {
        try {
            GameView gameView = new GameView();
            GameController controller = gameView.getGameController();
            // 🔹 Cargar estado
            controller.loadGame(player, machine, data);
            controller.setPlacementPhase(data.isPlacementPhase());
            // 🔹 Guardar al cerrar
            controller.attachCloseHandler(gameView);
            // 🔹 Mostrar juego
            gameView.show();
            // 🔹 Cerrar Welcome
            Stage welcomeStage = (Stage) btnNew.getScene().getWindow();
            welcomeStage.close();

        } catch (Exception e) {
            System.err.println("Error al intentar abrir la ventana del juego.");
            e.printStackTrace();
        }
    }

    /**
     * Manejador de evento para el botón de Ayuda. Llama a la función para mostrar
     * el diálogo de instrucciones.
     */
    @FXML
    /**
     * Muestra una alerta con las instrucciones del juego.
     */
    private void showInstructionsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instrucciones de Batalla Naval");
        alert.setHeaderText("¡Bienvenido al campo de batalla, Almirante!");

        String content = """
        🌊 FASE 1: COLOCACIÓN DE BARCOS
        
        • Flota: Tienes 10 barcos de diferentes tamaños:
            - 1 Acorazado (4 celdas)
            - 2 Cruceros (3 celdas)
            - 3 Destructores (2 celdas)
            - 4 Submarinos (1 celda)
            
        • Posicionamiento: Arrastra los barcos desde el panel izquierdo a tu tablero.
        • Rotación: Usa el botón "Rotar" o clic derecho para girar el barco.
        • Opciones: Usa "Organizar" para colocación automática.
        • Inicio: El botón "Iniciar Batalla" se habilitará al colocar los 10 barcos.

        💥 FASE 2: BATALLA
        
        • Tu Turno: Haz clic en el tablero enemigo (derecha) para disparar.
            💧 Agua (Miss): Se marca con una 'X'. Turno de la IA.
            💣 Impacto (Hit): Se marca con una bomba. ¡Repites turno!
            🚢 Hundido (Sunk): El barco arde en llamas. ¡Repites turno!
            
        • Turno de la IA: Si fallas, la máquina dispara a tu tablero.
        • Fin del Juego: Gana quien hunda toda la flota enemiga primero.
        """;

        alert.setContentText(content);

        // 2. Vincular el CSS
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/batallanaval/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        // 3. Icono propio
        try {
            ImageView icon = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/example/batallanaval/barco_icon.png")));
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            alert.setGraphic(icon);
        } catch (Exception ignored) { }

        // 4. AJUSTE DE TAMAÑO Y TEXTO
        dialogPane.setMinWidth(600);
        dialogPane.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        // Ajuste de línea
        javafx.scene.control.Label contentLabel = (javafx.scene.control.Label) dialogPane.lookup(".content");
        if (contentLabel != null) {
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #DFF3FF;"); // Forzar estilo legible
        }

        alert.setResizable(true);
        alert.showAndWait();
    }
}
