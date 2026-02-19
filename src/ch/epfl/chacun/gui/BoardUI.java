package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.*;
import java.util.function.Consumer;

/**
 * Interface utilisateur du plateau de jeu.
 *
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */

public final class BoardUI {
    private BoardUI(){}

    /**
     * Crée et retourne un nœud représentant le plateau de jeu.
     *
     * @param reach la portée du plateau
     * @param gameState l'état observable du jeu
     * @param tileRotation la rotation observable des tuiles
     * @param visibleOccupants les occupants visibles observables
     * @param tilesIds les identifiants des tuiles observables
     * @param rotationManager le gestionnaire de rotation
     * @param posManager le gestionnaire de position
     * @param occupantManager le gestionnaire d'occupants
     * @return un nœud représentant le plateau de jeu
     */
    public static  Node create(int reach, ObservableValue<GameState> gameState, ObservableValue<Rotation> tileRotation,
                              ObservableValue<Set<Occupant>> visibleOccupants, ObservableValue<Set<Integer>> tilesIds,
                               Consumer<Rotation> rotationManager, Consumer<Pos> posManager, Consumer<Occupant> occupantManager){

        GridPane gridPane = new GridPane();
        gridPane.setId("board-grid");

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setId("board-scroll-pane");
        scrollPane.getStylesheets().add("board.css");
        scrollPane.setVvalue(0.5);
        scrollPane.setHvalue(0.5);

        Map<Integer, Image> cache = new HashMap<>();

        int length = reach * 2 + 1;
        ObservableValue<Board> boardObs = gameState.map(GameState::board);

        for(int x = 0; x < length; x++){
            for (int y = 0; y < length; y++) {
                Group group = new Group();

                Pos pos  = new Pos(x - reach,y - reach);
                ObservableValue<PlacedTile> placedTileObs = boardObs.map(e -> e.tileAt(pos));

                ImageView tileImage = new ImageView();
                tileImage.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                tileImage.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);

                WritableImage emptyImageTile = new WritableImage(1,1);
                emptyImageTile
                        .getPixelWriter()
                        .setColor(0,0,Color.gray(0.98));
                tileImage.setImage(emptyImageTile);


                ObservableValue<CellData> cellDataObs  = Bindings.createObjectBinding(() -> {//Bind les valeurs du cellData
                    PlacedTile placedTileValue = placedTileObs.getValue();

                    CellData c = new CellData(emptyImageTile,0,null);

                    if(placedTileValue != null){
                        Image im = cache.computeIfAbsent(placedTileValue.id(), ImageLoader::normalImageForTile);
                        c = new CellData(im, placedTileValue.rotation().degreesCW(), null);

                        List<Node> markers = markers(boardObs.map(Board::cancelledAnimals), placedTileValue);
                        List<Node> occupants = occupants(placedTileValue, visibleOccupants,occupantManager);

                        occupants.forEach(occupant -> {
                            occupant.rotateProperty().bind(placedTileObs.map(r -> r.rotation().negated().degreesCW()));
                        });

                        group.getChildren().addAll(markers);
                        group.getChildren().addAll(occupants);

                        if(!tilesIds.getValue().contains(placedTileValue.id()) && !tilesIds.getValue().isEmpty()){
                            c = new CellData(im,placedTileValue.rotation().degreesCW(), Color.BLACK);
                        }

                        

                    }

                    if(boardObs.getValue().insertionPositions().contains(pos) && gameState.getValue().currentPlayer() != null){
                        Color color = ColorMap.fillColor(gameState.getValue().currentPlayer());

                        c = new CellData(emptyImageTile,tileRotation.getValue().degreesCW(),color);

                        if(group.hoverProperty().getValue() && gameState.getValue().tileToPlace() != null){
                            Image nextImage = cache.computeIfAbsent(gameState.getValue().tileToPlace().id(),
                                    ImageLoader::normalImageForTile);

                            PlacedTile tileToPlace = new PlacedTile(gameState.getValue().tileToPlace(),
                                    gameState.getValue().currentPlayer(), tileRotation.getValue(),pos);

                            color = boardObs.getValue().canAddTile(tileToPlace) ? null : Color.WHITE;
                            c = new CellData(nextImage,tileRotation.getValue().degreesCW(),color);
                        }
                    }

                  return c;
                },boardObs, tilesIds, tileRotation, group.hoverProperty(),placedTileObs,gameState,visibleOccupants);

                group.rotateProperty().bind(cellDataObs.map(CellData::rotationDegree));
                tileImage.imageProperty().bind(cellDataObs.map(CellData::image));
                group.getChildren().add(tileImage);


                group.setOnMouseClicked(e -> {
                    if(e.isStillSincePress()){
                        if(gameState.getValue().nextAction() == GameState.Action.PLACE_TILE && e.getButton() == MouseButton.PRIMARY){
                            posManager.accept(pos);
                        }
                        if(e.getButton() == MouseButton.SECONDARY){
                            Rotation rotation = e.isAltDown() ? Rotation.RIGHT : Rotation.LEFT;
                            rotationManager.accept(rotation);
                        }
                    }
                });


                ColorInput colorInput = new ColorInput();
                colorInput.setWidth(tileImage.getFitWidth());
                colorInput.setHeight(tileImage.getFitHeight());
                Blend blendEffect = new Blend(BlendMode.SRC_OVER,null,colorInput);
                blendEffect.setOpacity(0.5);

                group.effectProperty().bind(cellDataObs.map(e ->{
                    colorInput.setPaint(e.color());
                    return e.color() == null ? new Blend() : blendEffect;
                }));


                tileImage.imageProperty().bind(cellDataObs.map(CellData::image));
                gridPane.add(group, x, y);
                scrollPane.setContent(gridPane);
            }
        }

        return scrollPane;
    }

    //crée une liste de noeuds représentant les croix sur les animaux annulés du plateau.
    private static List<Node> markers(ObservableValue<Set<Animal>> cancelledAnimals, PlacedTile tile){
        List<Node> listOfMarker= new ArrayList<>();
        if(tile == null){
            return new ArrayList<>();
        }
        tile.meadowZones().forEach(meadow -> meadow.animals()
                .forEach(animal ->{
                    ImageView marker = new ImageView();
                    marker.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
                    marker.setFitHeight(ImageLoader.MARKER_FIT_SIZE);

                    marker.setId(STR."marker_\{animal.id()}");
                    marker.getStyleClass().add("marker");
                    marker.visibleProperty().bind(cancelledAnimals.map(s -> s.contains(animal)));
                    listOfMarker.add(marker);
                }));

        return listOfMarker;
    }

    //crée une liste de noeuds représentant les occupants du plateau
    private static List<Node> occupants(PlacedTile tile,
                                        ObservableValue<Set<Occupant>> visibleOccupants, Consumer<Occupant> occupantManager ){
        List<Node> occupantsList = new ArrayList<>();
        if(tile == null){
            return new ArrayList<>();
        }
        tile.potentialOccupants().forEach(occupant -> {
            Node occNode = Icon.newFor(tile.placer(), occupant.kind());

            switch (occupant.kind()){
                case Occupant.Kind.PAWN -> occNode.setId(STR."pawn_\{occupant.zoneId()}");
                case Occupant.Kind.HUT -> occNode.setId(STR."hut_\{occupant.zoneId()}");
            }
            occNode.visibleProperty().bind(visibleOccupants.map(v -> v.contains(occupant)));
            occNode.setOnMouseClicked(e ->{
                if(e.getButton() == MouseButton.PRIMARY) occupantManager.accept(occupant);
            });

            occupantsList.add(occNode);
        });

        return occupantsList;
    }


    /**
     * La représentation graphique d'une case.
     *
     * @param image L'image de fond de la case.
     * @param rotationDegree L'éventuelle rotation appliquée à la case.
     * @param color La couleur de l'éventuel voile recouvrant la case.
     */
    private record CellData(Image image, int rotationDegree, Color color){ }



}
