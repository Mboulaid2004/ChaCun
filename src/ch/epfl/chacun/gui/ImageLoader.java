package ch.epfl.chacun.gui;
import javafx.scene.image.Image;
import static java.util.FormatProcessor.FMT;

/**
 * A pour but de charger les images des tuiles.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class ImageLoader {

    /**
     * Taille des grandes tuiles.
     */
    public static final int LARGE_TILE_PIXEL_SIZE = 512;

    /**
     * Taille d'affichage des grandes tuiles.
     */
    public static final int LARGE_TILE_FIT_SIZE = 256;

    /**
     * Taille des tuiles normales
     */
    public static final int NORMAL_TILE_PIXEL_SIZE = 256;

    /**
     * Taille d'affichage des tuiles normales.
     */
    public static final int NORMAL_TILE_FIT_SIZE = 128;

    /**
     * Taille du marqueur
     */
    public static final int MARKER_PIXEL_SIZE = 96;

    /**
     * Taille d'affichage du marqueur
     */
    public static final int MARKER_FIT_SIZE = MARKER_PIXEL_SIZE / 2;

    private ImageLoader() {}

    /**
     * Retourne une valeur de type Image de 256 pixels.
     *
     * @param tileId Un identifiant de tuile.
     * @return L'image de 256 pixels de côté de la face de cette tuile.
     */
    public static Image normalImageForTile(int tileId) {
        String newTileId = FMT."%02d\{tileId}";
        return new Image(FMT."/\{NORMAL_TILE_PIXEL_SIZE}/\{newTileId}.jpg");
    }

    /**
     * Retourne une valeur de type Image de 512 pixels.
     *
     * @param tileId Un identifiant de tuile.
     * @return L'image de 512 pixels de côté de la face de cette tuile.
     */
    public static Image largeImageForTile(int tileId) {
        String newTileId = FMT."%02d\{tileId}";
        return new Image(STR."/\{LARGE_TILE_PIXEL_SIZE}/\{newTileId}.jpg");
    }
}