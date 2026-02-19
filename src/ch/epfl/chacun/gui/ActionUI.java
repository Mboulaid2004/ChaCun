package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Interface graphique des actions effectuées pendant la partie.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class ActionUI {

    private ActionUI() {}

    private final static int LAST_ACTIONS = 4; //Le nombre d'actions à afficher

    /**
     * Crée et retourne un interface graphique représentant les actions du jeu.
     *
     * @param actionsObservable la liste observable de toutes actions effectuées depuis le de la partie.
     * @param actionHandler gestionnaire d'actions.
     * @return un noeud représentant les actions du jeu.
     */
    public static Node create(ObservableValue<List<String>> actionsObservable, Consumer<String> actionHandler) {
        HBox root = new HBox();
        root.setId("actions");
        root.getStylesheets().add("actions.css");
        Text actionsDisplay = new Text();
        actionsObservable.addListener((_, _, newVal) -> {
            String actionsText = newVal.stream()
                    .skip(doSkip(newVal))
                    .map(action -> STR."\{newVal.indexOf(action) + 1}:\{action}")
                    .collect(Collectors.joining(", "));
            actionsDisplay.setText(actionsText);
        });


        TextField actionField = new TextField();
        actionField.setId("action-field");
        actionField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().toUpperCase());
            return change.getControlNewText().chars().allMatch(c -> Base32.ALPHABET.indexOf(c) >= 0) ? change : null;
        }));


        actionField.setOnAction(_ -> {
            String text = actionField.getText();
            if (!text.isEmpty() && Base32.isValid(text)) {
                actionHandler.accept(text);
                actionField.clear();
            }
        });

        root.getChildren().addAll(actionsDisplay, actionField);
        return root;
    }

    private static int doSkip(List<String> list){ //Gère l'affichage de seulement 4 actions en simultané
        if(list.size() < LAST_ACTIONS) return 0;
        return list.size() - LAST_ACTIONS;
    }
}