package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Permet d'encoder et de décoder des (paramètres) d'actions, et d'appliquer ces actions à un état de jeu..
 *
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class ActionEncoder {


    private ActionEncoder() {}

    /**
     * L'état de jeu correspondant à l'action en base32 et vice-versa.
     *
     * @param gameState Un état de jeu, qui est celui résultant de l'application d'une action à un état de jeu initial.
     * @param action Une chaîne de caractère qui est l'encodage, en base32, de l'action appliquée.
     */
    public record StateAction(GameState gameState, String action) {}

    private final static int ENCODED_VALUE = 0b11111;

    /**
     * La paire d'état et d'action de jeu en ajoutant la tuile donnée au plateau.
     *
     * @param gameState L'état de jeu avant modification.
     * @param tile La tuile à placer sur le plateau.
     * @return Le nouvel état de jeu et l'action en base32.
     */
    public static StateAction withPlacedTile(GameState gameState, PlacedTile tile) {
        GameState updatedState = gameState.withPlacedTile(tile);
        List<Pos> sortedPositions = gameState.board().insertionPositions().stream()
                .sorted((p1, p2) -> {
                    if (p1.x() != p2.x()) {
                        return Integer.compare(p1.x(), p2.x());
                    } else {
                        return Integer.compare(p1.y(), p2.y());
                    }
                })
                .toList();

        int locationIndex = sortedPositions.indexOf(tile.pos());
        int tileOrientation = tile.rotation().ordinal();
        int tileDetails = (locationIndex << 2) | tileOrientation;
        String encodedAction = Base32.encodeBits10(tileDetails);

        return new StateAction(updatedState, encodedAction);
    }

    /**
     * La paire d'état et d'action de jeu à l'ajout d'un nouvel occupant.
     *
     * @param gameState L'état de jeu avant modification.
     * @param occupant L'occupant à placer.
     * @return Le nouvel état de jeu et l'action en base32
     */
    public static StateAction withNewOccupant(GameState gameState, Occupant occupant) {
        GameState newState = gameState.withNewOccupant(occupant);
        int encodedValue;
        if (occupant == null) {
            encodedValue = ENCODED_VALUE;
        } else {
            if (occupant.kind() == Occupant.Kind.PAWN && occupant.zoneId() == 10) {
                encodedValue = 0;
            }
            else if (occupant.kind() == Occupant.Kind.HUT && occupant.zoneId() == 18) {
                encodedValue = 0b11000;
            }
            else {
                encodedValue = (occupant.kind().ordinal() << 4) | occupant.zoneId();
            }
        }
        String encodedAction = Base32.encodeBits5(encodedValue);
        return new StateAction(newState, encodedAction);
    }

    /**
     * Supprime un occupant de l'état de jeu.
     *
     * @param gameState L'état de jeu avant modification.
     * @param occupant L'occupant à supprimer.
     * @return Le nouvel état de jeu et l'action en base32.
     */
    public static StateAction withOccupantRemoved(GameState gameState, Occupant occupant) {
        GameState nextState = gameState.withOccupantRemoved(occupant);
        int encodedValue = (occupant == null) ? ENCODED_VALUE : Zone.localId(occupant.zoneId());
        String encodedAction = Base32.encodeBits5(encodedValue);
        return new StateAction(nextState, encodedAction);
    }

    /**
     * La paire d'état et d'action de jeu décodée.
     *
     * @param gameState L'état de jeu à modifier.
     * @param action L'action à appliquer.
     * @return La paire composée de l'état du jeu résultant de l'application de l'action en argument,
     * et de la chaîne de caractères représentant l'action.
     */
    public static StateAction decodeAndApply(GameState gameState, String action) {
        StateAction result = null;
        try {
            result = processDecoding(gameState, action);
        } catch (RuntimeException _) {
        }
        return result;
    }

    private static StateAction processDecoding(GameState gameState, String action) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(action, "Action string cannot be null");
        Preconditions.checkArgument(Base32.isValid(action));

        GameState.Action futureAction = gameState.nextAction();
        int decodedValue = Base32.decode(action);
        GameState newState = switch (futureAction) {
            case PLACE_TILE -> applyPlaceTile(gameState, decodedValue);
            case OCCUPY_TILE -> applyOccupyTile(gameState, decodedValue);
            case RETAKE_PAWN -> applyRetakePawn(gameState, decodedValue);
            default -> throw new IllegalArgumentException(STR."Unsupported action type: \{futureAction}");
        };
        return new StateAction(newState, action);
    }

    private static GameState applyPlaceTile(GameState gameState, int decodedParameters) {
        int positionIndex = decodedParameters >> 2;
        Rotation rotation = Rotation.values()[decodedParameters & 0b11];
        List<Pos> sortedPositions = gameState.board().insertionPositions().stream()
                .sorted(Comparator.comparingInt(Pos::x).thenComparingInt(Pos::y))
                .toList();
        Pos position = sortedPositions.get(positionIndex);
        PlacedTile tile = new PlacedTile(gameState.tileToPlace(), gameState.currentPlayer(), rotation, position);
        return gameState.withPlacedTile(tile);
    }

    private static GameState applyOccupyTile(GameState gameState, int decodedParameters) {
        Occupant.Kind kind = Occupant.Kind.values()[decodedParameters >> 4];
        int zoneId = decodedParameters & ENCODED_VALUE;
        Occupant occupant = new Occupant(kind, gameState.board().lastPlacedTile().id() * 10 + zoneId);
        return gameState.withNewOccupant(occupant);

    }

    private static GameState applyRetakePawn(GameState gameState, int decodedParameters) {
        int zoneId = decodedParameters & ENCODED_VALUE;
        List<Occupant> occupants = gameState.board().occupants().stream()
                .sorted(Comparator.comparingInt(Occupant ::zoneId)).toList();
        Occupant occupant = occupants.get(zoneId);
        return gameState.withOccupantRemoved(occupant);
    }
}
