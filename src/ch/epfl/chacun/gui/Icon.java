package ch.epfl.chacun.gui;
import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Représente les occupants des différents joueurs.
 *
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class Icon {
    private Icon() {}

    /**
     *
     * @param color Couleur de joueur.
     * @param kind Type d'occupant.
     * @return Une instance de {@code SVGPath} représentant l'occupant correspondant.
     */
    public static Node newFor(PlayerColor color, Occupant.Kind kind) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(getPathData(kind));
        svgPath.setFill(ColorMap.fillColor(color));
        svgPath.setStroke(ColorMap.strokeColor(color));
        return svgPath;
    }
    private static String getPathData(Occupant.Kind kind) {//retourne le svgPath pour un type d'occupant
        return switch (kind) {
            case PAWN -> "M -10 10 H -4 L 0 2 L 6 10 H 12 L 5 0 L 12 -2 L 12 -4 L 6 -6 L 6 -10" +
                    " L 0 -10 L -2 -4 L -6 -2 L -8 -10 L -12 -10 L -8 6 Z";
            case HUT -> "M -8 10 H 8 V 2 H 12 L 0 -10 L -12 2 H -8 Z";
        };
    }
}