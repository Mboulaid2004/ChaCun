
package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Preconditions;
import ch.epfl.chacun.Tile;
import ch.epfl.chacun.gui.ImageLoader;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * Interface graphique des tas de tuiles et la tuile à poser.
 *
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class DecksUI {
    private DecksUI() {}

    /**
     * Crée un noeud représentant les tas de tuiles et la tuile à placer.
     *
     * @param tileToPlaceObs La version observable de la tuile à placer.
     * @param countNormalTiles La version observable du nombre de tuiles restantes dans le tas des tuiles normales.
     * @param countMenhirTiles La version observable du nombre de tuiles restantes dans le tas des tuiles menhir.
     * @param textDisplay La version observable du texte à afficher à la place de la tuile à placer.
     * @param eventManager Le gestionnaire d'évenements qui signale le fait de poser ou de reprendre un occupant.
     * @return Un noeud représentant les tas de tuiles et la tuile à placer.
     */
    public static Node create(ObservableValue<Tile> tileToPlaceObs, ObservableValue<Integer> countNormalTiles, ObservableValue<Integer> countMenhirTiles,
                              ObservableValue<String> textDisplay, Consumer<Occupant> eventManager) {

        VBox vbox = new VBox();
        vbox.getStylesheets().add("decks.css");

        HBox SetOfDecks = new HBox();
        SetOfDecks.setId("decks");
        //Creation des StackPanes pour les tuiles NORMAL et MENHIR
        StackPane normalTiles = createStackPane(countNormalTiles,"NORMAL");
        SetOfDecks.getChildren().add(normalTiles);

        StackPane menhirTiles = createStackPane(countMenhirTiles, "MENHIR");
        SetOfDecks.getChildren().add(menhirTiles);

        vbox.getChildren().add(SetOfDecks);

        StackPane nextTile = new StackPane();
        ImageView followingTileImage = new ImageView();
        Text followingTileText = new Text();

        nextTile.setId("next-tile");
        followingTileImage.imageProperty().bind(tileToPlaceObs.map(t -> ImageLoader.largeImageForTile(t.id())));
        followingTileImage.setFitHeight(ImageLoader.NORMAL_TILE_PIXEL_SIZE);
        followingTileImage.setFitWidth(ImageLoader.NORMAL_TILE_PIXEL_SIZE);

        nextTile.getChildren().add(followingTileImage);

        followingTileText.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE * 0.8);
        followingTileText.textProperty().bind(textDisplay);
        followingTileText.visibleProperty().bind(textDisplay.map(s -> !s.isEmpty()));

        nextTile.setOnMouseClicked(_ -> {
            if(followingTileText.isVisible())
                eventManager.accept(null);
        });


        nextTile.getChildren().add(followingTileText);

        vbox.getChildren().add(nextTile);
        return vbox;
    }

    //Crée le StackPane pour un tas de tuiles Menhir ou Normal
    private static StackPane createStackPane(ObservableValue<Integer> countTiles, String tileKind) {
        StackPane stackPane = new StackPane();
        ImageView imageView = new ImageView();

        imageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        imageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        imageView.setId(tileKind);

        Text tilesText = new Text();
        tilesText.textProperty().bind(Bindings.convert(countTiles));
        stackPane.getChildren().addAll(imageView, tilesText);

        return stackPane;
    }


}