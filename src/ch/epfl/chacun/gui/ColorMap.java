package ch.epfl.chacun.gui;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.Preconditions;
import javafx.scene.paint.Color;

/**
 * Représente à l'écran les cinq couleurs de joueur qui existent dans ChaCuN.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class ColorMap {

    private ColorMap() {}

    /**
     * Permets de remplir les occupants d'une couleur donnée.
     *
     * @param color La couleur du joueur.
     * @return La couleur JavaFX de type {@code javafx.scene.paint.Color} des occupants du joueur donné.
     */
    public static Color fillColor(PlayerColor color) {
        return switch (color) {
            case RED -> Color.RED;
            case BLUE -> Color.BLUE;
            case GREEN -> Color.LIME;
            case YELLOW -> Color.YELLOW;
            case PURPLE -> Color.PURPLE;
        };
    }

    /**
     * Dessine les countours des occupants.
     *
     * @param color La couleur du joueur.
     * @return La couleur JavaFX de type {@code javafx.scene.paint.Color} des contours des occupants du joueur donné.
     */
    public static Color strokeColor(PlayerColor color) {
        return switch (color) {
            case YELLOW, GREEN ->
                    fillColor(color).deriveColor(0, 1, 0.6, 1);
            default -> Color.WHITE;
        };
    }
}