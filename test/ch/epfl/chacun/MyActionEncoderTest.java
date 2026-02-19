package ch.epfl.chacun;

import ch.epfl.chacun.Tiles;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MyActionEncoderTest {

    private final List<PlayerColor> playersList = List.of(PlayerColor.RED, PlayerColor.BLUE);

    private final Tile tile0 = Tiles.TILES.getFirst();
    private final Tile tile1 = Tiles.TILES.get(1);
    private final Tile tile2 = Tiles.TILES.get(2);
    private final Tile tile25 = Tiles.TILES.get(25);
    private final Tile tile27 = Tiles.TILES.get(27);
    private final Tile tile31 = Tiles.TILES.get(31);
    private final Tile tile33 = Tiles.TILES.get(33);
    private final Tile tile35 = Tiles.TILES.get(35);
    private final Tile tile37 = Tiles.TILES.get(37);
    private final Tile tile44 = Tiles.TILES.get(44);
    private final Tile tile45 = Tiles.TILES.get(45);
    private final Tile tile47 = Tiles.TILES.get(47);
    private final Tile tile49 = Tiles.TILES.get(49);
    private final Tile tile54 = Tiles.TILES.get(54);
    private final Tile tile56 = Tiles.TILES.get(56);
    private final Tile tile62 = Tiles.TILES.get(62);
    private final Tile tile65 = Tiles.TILES.get(65);
    private final Tile tile71 = Tiles.TILES.get(71);
    private final Tile tile72 = Tiles.TILES.get(72);
    private final Tile tile88 = Tiles.TILES.get(88);
    private final Tile tile90 = Tiles.TILES.get(90);

    @Test
    void withPlacedTileWorks() {
        //TODO edge cases ?
        var start = List.of(tile56);
        var normal = List.of(tile0, tile1, tile2);
        var menhir = List.of(tile88, tile90);
        var tileDecks = new TileDecks(start, normal, menhir);
        Map<PlayerColor, String> players = new HashMap<>();
        players.put(PlayerColor.RED, "Rose");
        players.put(PlayerColor.BLUE, "Bernard");
        var textMaker = new TextMakerFr(players);
        var gameState = GameState.initial(playersList, tileDecks, textMaker);

        var pt0 = new PlacedTile(tile0, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        var pt1 = new PlacedTile(tile1, PlayerColor.BLUE, Rotation.RIGHT, new Pos(1, -1));

        var gameState1 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0)
                .withNewOccupant(null);
        var gameState2 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0)
                .withNewOccupant(null);

        var expectedGS1 = gameState1.withPlacedTile(pt1);
        var expectedGS2 = gameState2.withPlacedTile(pt1);

        var value = 0b00000_01101;
        var expectedAction = Base32.encodeBits10(value);
        var expectedSA1 = new ActionEncoder.StateAction(expectedGS1, expectedAction);
        var expectedSA2 = new ActionEncoder.StateAction(expectedGS2, "AN");

        assertAll(
                () -> assertEquals(expectedSA1, ActionEncoder.withPlacedTile(gameState1, pt1)),
                () -> assertEquals(expectedSA2, ActionEncoder.withPlacedTile(gameState2, pt1))
        );
    }

    @Test
    void withNewOccupantWorksWithNullOccupant() {
        var start = List.of(tile56);
        var normal = List.of(tile0, tile1, tile2);
        var menhir = List.of(tile88, tile90);
        var tileDecks = new TileDecks(start, normal, menhir);
        Map<PlayerColor, String> players = new HashMap<>();
        players.put(PlayerColor.RED, "Rose");
        players.put(PlayerColor.BLUE, "Bernard");
        var textMaker = new TextMakerFr(players);
        var gameState = GameState.initial(playersList, tileDecks, textMaker);

        var pt0 = new PlacedTile(tile0, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));

        var gameState1 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0);
        var expectedGS = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0)
                .withNewOccupant(null);

        var expectedSA1 = new ActionEncoder.StateAction(expectedGS, Base32.encodeBits5(0b11111));
        var expectedSA2 = new ActionEncoder.StateAction(expectedGS, "7");

        assertAll(
                () -> assertEquals(expectedSA1, ActionEncoder.withNewOccupant(gameState1, null)),
                () -> assertEquals(expectedSA2, ActionEncoder.withNewOccupant(gameState1, null))
        );
    }

    @Test
    void withNewOccupantWorksWithNonNullOccupant() {
        var pawn010 = new Occupant(Occupant.Kind.PAWN, 10);
        var hut018 = new Occupant(Occupant.Kind.HUT, 18);

        var start = List.of(tile56);
        var normal = List.of(tile0, tile1, tile2);
        var menhir = List.of(tile88, tile90);
        var tileDecks = new TileDecks(start, normal, menhir);
        Map<PlayerColor, String> players = new HashMap<>();
        players.put(PlayerColor.RED, "Rose");
        players.put(PlayerColor.BLUE, "Bernard");
        var textMaker = new TextMakerFr(players);
        var gameState = GameState.initial(playersList, tileDecks, textMaker);

        var pt0 = new PlacedTile(tile0, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        var pt1 = new PlacedTile(tile1, PlayerColor.BLUE, Rotation.RIGHT, new Pos(1, -1));

        var gameState1 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0)
                .withNewOccupant(null)
                .withPlacedTile(pt1);
        var gameState2 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0)
                .withNewOccupant(null)
                .withPlacedTile(pt1);

        var expectedGS1 = gameState1.withNewOccupant(pawn010);
        var expectedGS2 = gameState2.withNewOccupant(hut018);

        var expectedSA1 = new ActionEncoder.StateAction(expectedGS1, Base32.encodeBits5(0));
        var expectedSA2 = new ActionEncoder.StateAction(expectedGS2, Base32.encodeBits5(0b11000));

        assertAll(
                () -> assertEquals(expectedSA1, ActionEncoder.withNewOccupant(gameState1, pawn010)),
                () -> assertEquals(expectedSA2, ActionEncoder.withNewOccupant(gameState2, hut018))
        );
    }

    @Test
    void withOccupantRemovedWorksOnNullOccupant() {
        var start = List.of(tile56);
        var normal = List.of(tile0, tile1, tile2);
        var menhir = List.of(tile88, tile90);
        var tileDecks = new TileDecks(start, normal, menhir);
        Map<PlayerColor, String> players = new HashMap<>();
        players.put(PlayerColor.RED, "Rose");
        players.put(PlayerColor.BLUE, "Bernard");
        var textMaker = new TextMakerFr(players);
        var gameState = GameState.initial(playersList, tileDecks, textMaker);

        var pt0 = new PlacedTile(tile0, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        var pt1 = new PlacedTile(tile1, PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(0, 1));
        var pt88 = new PlacedTile(tile88, PlayerColor.BLUE, Rotation.NONE, new Pos(-1, 0));

        var pawn002 = new Occupant(Occupant.Kind.PAWN, 2);
        var pawn014 = new Occupant(Occupant.Kind.PAWN, 14);

        var gameState1 = gameState.withStartingTilePlaced()
                .withPlacedTile(pt0).withNewOccupant(pawn002)
                .withPlacedTile(pt1).withNewOccupant(pawn014)
                .withPlacedTile(pt88);

        var expectedGS1 = gameState1.withOccupantRemoved(pawn002);
    }
}