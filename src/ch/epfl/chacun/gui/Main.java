package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.security.auth.login.AccountExpiredException;
import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Programme principal du jeu.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117x)
 */
public final class Main extends Application {

    /**
     * Lance le jeu avec les paramètres specifiés.
     * @param args Les arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Crée l'état de jeu initial et son interface graphique.
     *
     * @param primaryStage La scène principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        var parameters = getParameters();
        var unnamedArgs = parameters.getUnnamed();
        var namedArgs = parameters.getNamed();

        // On s'assure qu'il y a au moins 2 joueurs
        Preconditions.checkArgument(unnamedArgs.size() > 1 && unnamedArgs.size() <= 5);

        // On mets les joueurs en argument dans une liste de PlayerColor
        List<PlayerColor> playerColors = Arrays.stream(PlayerColor.values()).sorted().toList();
        List<PlayerColor> players = new ArrayList<>();
        IntStream.range(0,unnamedArgs.size())
                .forEach(i -> players.add(playerColors.get(i)));


        // On prend la seed value si elle nous est donnée
        long seed = namedArgs.containsKey("seed") ? Long.parseUnsignedLong(namedArgs.get("seed")) : new Random().nextLong();

        var randomGenerator = RandomGeneratorFactory.getDefault().create(seed);

        var tiles = new ArrayList<>(Tiles.TILES);
        Collections.shuffle(tiles, randomGenerator);
        var tilesByKind = tiles.stream().collect(Collectors.groupingBy(Tile::kind));

        var tileDecks = new TileDecks(
                tilesByKind.get(Tile.Kind.START),
                tilesByKind.get(Tile.Kind.NORMAL),
                tilesByKind.getOrDefault(Tile.Kind.MENHIR,List.of())
        );


        // On map le nom des joueurs à leur couleur respective
        var playerNames = new HashMap<PlayerColor, String>();
        IntStream.range(0, unnamedArgs.size())
                .forEach(i -> playerNames.put(players.get(i), unnamedArgs.get(i)));


        var textMaker = new TextMakerFr(playerNames);

        // On initialise le game state
        var gameState = GameState.initial(players, tileDecks, textMaker);
        var gameState0 = new SimpleObjectProperty<>(gameState);


        // on crée les observables
        var messageList = gameState0.map(gs -> gs.messageBoard().messages());
        var tileToPlace = gameState0.map(GameState::tileToPlace);
        var normalCount = gameState0.map(gs -> gs.tileDecks().deckSize(Tile.Kind.NORMAL));
        var menhirCount = gameState0.map(gs -> gs.tileDecks().deckSize(Tile.Kind.MENHIR));


        var text = gameState0.map(gs -> switch (gs.nextAction()) {
            case OCCUPY_TILE -> textMaker.clickToOccupy();
            case RETAKE_PAWN -> textMaker.clickToUnoccupy();
            default -> "";
        });

        var actions = new SimpleObjectProperty<List<String>>(new ArrayList<>());
        var tileToPlaceRotationP = new SimpleObjectProperty<>(Rotation.NONE);


        SimpleObjectProperty<Set<Occupant>> visibleOccupantsP = new SimpleObjectProperty<>(Set.of());

        visibleOccupantsP.bind(gameState0.map(g ->{
            Set<Occupant> potentialOccupants = new HashSet<>(g.board().occupants());
            if (g.nextAction() == GameState.Action.OCCUPY_TILE){
                potentialOccupants.addAll(g.lastTilePotentialOccupants());
            }
            return potentialOccupants;
        }));

        var tileIds = new SimpleObjectProperty<>(Set.<Integer>of());


        // On définit les consumers
        Consumer<Occupant> occupantConsumer = e ->{
            if(gameState0.get().nextAction() == GameState.Action.OCCUPY_TILE){
                if(e == null){
                    ActionEncoder.StateAction stateAction = ActionEncoder.withNewOccupant(gameState0.get(), e);
                    actions.setValue(actionList(stateAction,actions));

                    gameState0.set(gameState0.get().withNewOccupant(null));
                }
                else{
                    PlacedTile tile = gameState0.get().board().tileWithId(Zone.tileId(e.zoneId()));

                    if(tile.placer() == gameState0.get().currentPlayer()){
                        ActionEncoder.StateAction stateAction = ActionEncoder.withNewOccupant(gameState0.get(), e);
                        actions.setValue(actionList(stateAction,actions));

                        gameState0.set(gameState0.get().withNewOccupant(e));
                    }
                }
            }

            if(gameState0.get().nextAction() == GameState.Action.RETAKE_PAWN){
                if(e == null) {
                    gameState0.set(gameState0.get().withOccupantRemoved(null));
                    ActionEncoder.StateAction stateAction = ActionEncoder.withOccupantRemoved(gameState0.get(), null);
                    actions.setValue(actionList(stateAction,actions));
                }

                else{
                    PlacedTile tile = gameState0.get().board().tileWithId(Zone.tileId(e.zoneId()));

                    if(tile.placer() == gameState0.get().currentPlayer() && e.kind() == Occupant.Kind.PAWN){
                        ActionEncoder.StateAction stateAction = ActionEncoder.withOccupantRemoved(gameState0.get(), e);
                        actions.setValue(actionList(stateAction,actions));
                        gameState0.set(gameState0.get().withOccupantRemoved(e));
                    }
                }
            }
        };

        Consumer<String> actionConsumer = string -> {
            var result = ActionEncoder.decodeAndApply(gameState0.get(), string);
            if (result != null) {
                actions.set(actionList(result,actions));
                gameState0.set(result.gameState());
            }
        };

        Consumer<Rotation> rotationConsumer = e -> {
            tileToPlaceRotationP.set(tileToPlaceRotationP.getValue().add(e));
        };


        Consumer<Pos> positionConsumer = e -> {
            PlacedTile tile = new PlacedTile(tileToPlace.getValue()
                    ,gameState0.get().currentPlayer(),tileToPlaceRotationP.getValue(),e);

            if(gameState0.get().nextAction() == GameState.Action.PLACE_TILE && gameState0.get().board().canAddTile(tile)){
                ActionEncoder.StateAction stateAction = ActionEncoder.withPlacedTile(gameState0.get(),tile);
                actions.setValue(actionList(stateAction,actions));

                gameState0.set(gameState0.get().withPlacedTile(tile));
                tileToPlaceRotationP.set(Rotation.NONE);
            }
        };

        // On crée les interfaces graphiques
        var playersNode = PlayersUI.create(gameState0, textMaker);
        var messagesNode = MessageBoardUI.create(messageList, tileIds);
        var actionsNode = ActionUI.create(actions, actionConsumer);
        var decksNode = DecksUI.create(tileToPlace, normalCount, menhirCount, text, occupantConsumer);
        var boardNode = BoardUI.create(Board.REACH,gameState0,tileToPlaceRotationP,visibleOccupantsP,
                tileIds,rotationConsumer,positionConsumer,occupantConsumer);

        var bottomVbox = new VBox();
        bottomVbox.getChildren().add(actionsNode);
        bottomVbox.getChildren().add(decksNode);

        var rightBorderPane = new BorderPane(messagesNode, playersNode, null, bottomVbox, null);
        var finalBorderPane = new BorderPane(boardNode,null,rightBorderPane,null,null);

        gameState0.set(gameState.withStartingTilePlaced());


        primaryStage.setTitle("ChaCuN");
        primaryStage.setHeight(1080);
        primaryStage.setWidth(1440);
        primaryStage.setScene(new Scene(finalBorderPane));
        primaryStage.show();

    }

    //gère les actions à mettre à jour
    private static List<String> actionList(ActionEncoder.StateAction stateActions, SimpleObjectProperty<List<String>> actionsOb){
        List<String> newActions = new ArrayList<>(actionsOb.get());
        newActions.add(stateActions.action());
        return newActions;
    }
}
