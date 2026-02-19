package ch.epfl.chacun.gui;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ch.epfl.chacun.MessageBoard;
import ch.epfl.chacun.MessageBoard.Message;

import java.util.List;
import java.util.Set;

import static javafx.application.Platform.runLater;

/**
 * Interface graphique du tableau d'affichage.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class MessageBoardUI {

    private static final double LARGE_TILE_FIT_SIZE = 256 * 0.8; //la taille de la tuile adaptée aux dimensions du tableau

    /**
     * Crée un noeud représentant le tableau d'affichage.
     * @param messageListObs La version observable des messages affichés sur le tableau d'affichage.
     * @param intSetObs L'ensemble des identités des tuiles à mettre en évidence sur le plateau.
     * @return un noeud représentant le tableau d'affichage.
     */
    public static Node create(ObservableValue<List<MessageBoard.Message>> messageListObs, ObjectProperty<Set<Integer>> intSetObs){
        VBox messageContainer = new VBox(10);
        ScrollPane messageScrollPane = new ScrollPane();

        messageScrollPane.setContent(messageContainer);
        messageScrollPane.setId("message-board");
        messageScrollPane.getStylesheets().add("message-board.css");
        messageScrollPane.setFitToWidth(true);

        messageListObs.addListener((_, _, newMessageList) -> {
            messageContainer.getChildren().clear();

            for (Message message : newMessageList) {
                Text messageText = new Text(message.text());

                messageText.setOnMouseEntered(_ ->
                        intSetObs.set(message.tileIds())
                );

                messageText.setOnMouseExited(_ ->
                        intSetObs.set(Set.of())
                );

                messageText.setWrappingWidth(LARGE_TILE_FIT_SIZE);
                messageContainer.getChildren().add(messageText);

            }
            messageScrollPane.layout();
            messageScrollPane.setVvalue(1.0);
        });

        return messageScrollPane;
    }
}