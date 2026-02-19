package ch.epfl.chacun.gui;
import ch.epfl.chacun.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Interface graphique des informations sur les joueurs.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class PlayersUI {

    /**
     * Crée l'interphace graphique des joueurs.
     * @param gameStateObservable La version observable de l'état actuel de la partie.
     * @param textMaker Un générateur de texte.
     * @return un noeud représentant l'interphace graphique des joueurs.
     */
    public static Node create(ObservableValue<GameState> gameStateObservable, TextMaker textMaker) {
        TreeMap<PlayerColor, TextFlow> playerDisplays = new TreeMap<>();

        gameStateObservable.getValue().players().forEach(p ->{
            Circle playerCircle = new Circle(5, ColorMap.fillColor(p));
            TextFlow playerInfo = new TextFlow(playerCircle);
            playerDisplays.put(p, playerInfo);
        });

        ObservableValue<Map<PlayerColor, Integer>> playerScores =
                gameStateObservable.map(state -> state.messageBoard().points());

        playerDisplays.keySet().forEach(e -> {
            ObservableValue<String> scoreText = playerScores.map(scores -> STR."\{textMaker.playerName(e)}: \{scores.getOrDefault(e, 0)} points \n");
            Text scoreDisplay = new Text();
            scoreDisplay.textProperty().bind(scoreText);
            playerDisplays.get(e).getStyleClass().add("player");
            playerDisplays.get(e).getChildren().add(scoreDisplay);

            ObservableValue<PlayerColor> currentPlayer = gameStateObservable.map(GameState::currentPlayer);

            currentPlayer.addListener((_, _, newState) -> {
                playerDisplays.forEach((p, flow) -> {
                    flow.getStyleClass().remove("current");
                    if (p == newState) {
                        flow.getStyleClass().add("current");
                    }
                });
            });
        });


        occupantRepresentation(playerDisplays,3, Occupant.Kind.HUT, gameStateObservable);
        occupantRepresentation(playerDisplays,5, Occupant.Kind.PAWN, gameStateObservable);


        VBox vbox = new VBox();
        vbox.setId("players");
        vbox.getStylesheets().add("players.css");
        vbox.getChildren().addAll(playerDisplays.values());
        return vbox;
    }

    //Crée les icones des pions et des huttes et les ajoute au TextFlow
    private static void occupantRepresentation(TreeMap<PlayerColor,TextFlow> playerDisplay, int i,
                                               Occupant.Kind kind, ObservableValue<GameState> gameStateObs){
        playerDisplay.forEach((playerColor, textFlow) -> {
            for (int j = 0; j < i; j++) {
                Node occupantIcon = Icon.newFor(playerColor, kind);
                int I = j;
                ObservableValue<Double> opacity = gameStateObs.map(e ->
                        e.freeOccupantsCount(playerColor,kind) > I ? 1.0 : 0.1);

                occupantIcon.opacityProperty().bind(opacity);
                textFlow.getChildren().add(occupantIcon);
            }

            textFlow.getChildren().add(new Text("   "));
        });

    }
}