package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.*;

import static ch.epfl.chacun.Animal.Kind.*;
import static ch.epfl.chacun.PlayerColor.*;
import static org.junit.jupiter.api.Assertions.*;

class TextMakerFrTests {

    private Map<PlayerColor, String> players() {
        Map<PlayerColor, String> playerColorStringMap = new HashMap<>();
        playerColorStringMap.put(BLUE, "Claude");
        playerColorStringMap.put(YELLOW, "Alice");
        playerColorStringMap.put(RED, "Dalia");
        playerColorStringMap.put(GREEN, "Bachir");
        return playerColorStringMap;
    }

    @Test
    void playerNameWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        String actualBlue = textMakerFr.playerName(BLUE);
        String expectedBlue = "Claude";
        String actualYellow = textMakerFr.playerName(YELLOW);
        String expectedYellow = "Alice";
        assertEquals(expectedBlue, actualBlue);
        assertEquals(expectedYellow, actualYellow);
    }

    @Test
    void pointsWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        String actualPoints = textMakerFr.points(12);
        String expectedPoints = "12 points";
        assertEquals(expectedPoints, actualPoints);
    }

    @Test
    void playerClosedForestWithMenhirWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        String actualBlue = textMakerFr.playerClosedForestWithMenhir(BLUE);
        String expectedBlue = "Claude a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.";
        String actualRed = textMakerFr.playerClosedForestWithMenhir(RED);
        String expectedRed = "Dalia a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.";
        assertEquals(expectedBlue, actualBlue);
        assertEquals(expectedRed, actualRed);
    }

    @Test
    void playersScoredForestWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);
        Set<PlayerColor> scorers2 = Set.of(GREEN);
        Set<PlayerColor> scorers3 = Set.of(YELLOW, BLUE);
        String actualMessage = textMakerFr.playersScoredForest(scorers, Points.forClosedForest(3, 2), 2, 3);
        String expectedMessage = "Dalia, Claude et Alice ont remporté 12 points en tant qu'occupant·e·s majoritaires d'une forêt composée de 3 tuiles et de 2 groupes de champignons.";
        String actualMessage2 = textMakerFr.playersScoredForest(scorers2, Points.forClosedForest(3, 0), 0, 3);
        String expectedMessage2 = "Bachir a remporté 6 points en tant qu'occupant·e majoritaire d'une forêt composée de 3 tuiles.";
        String actualMessage3 = textMakerFr.playersScoredForest(scorers3, Points.forClosedForest(4, 1), 1, 4);
        String expectedMessage3 = "Claude et Alice ont remporté 11 points en tant qu'occupant·e·s majoritaires d'une forêt composée de 4 tuiles et de 1 groupe de champignons.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
        assertEquals(expectedMessage3, actualMessage3);
    }

    @Test
    void playersScoredRiverWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);
        Set<PlayerColor> scorers2 = Set.of(GREEN);
        Set<PlayerColor> scorers3 = Set.of(YELLOW, BLUE);
        String actualMessage = textMakerFr.playersScoredRiver(scorers, Points.forClosedRiver(3, 2), 2, 3);
        String expectedMessage = "Dalia, Claude et Alice ont remporté 5 points en tant qu'occupant·e·s majoritaires d'une rivière composée de 3 tuiles et contenant 2 poissons.";
        String actualMessage2 = textMakerFr.playersScoredRiver(scorers2, Points.forClosedRiver(3, 0), 0, 3);
        String expectedMessage2 = "Bachir a remporté 3 points en tant qu'occupant·e majoritaire d'une rivière composée de 3 tuiles.";
        String actualMessage3 = textMakerFr.playersScoredRiver(scorers3, Points.forClosedRiver(4, 3), 3, 4);
        String expectedMessage3 = "Claude et Alice ont remporté 7 points en tant qu'occupant·e·s majoritaires d'une rivière composée de 4 tuiles et contenant 3 poissons.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
        assertEquals(expectedMessage3, actualMessage3);
    }

    @Test
    void playerScoredHuntingTrapWorks() {
        Random random = new Random();
        TextMakerFr textMakerFr = new TextMakerFr(players());
        TreeMap<Animal.Kind, Integer> animals = new TreeMap<>();
        for (int i = 0; i < Animal.Kind.values().length; i++) {
            animals.put(Animal.Kind.values()[i], random.nextInt(2, 4));
        }

        TreeMap<Animal.Kind, Integer> animals2 = new TreeMap<>();
        animals2.put(MAMMOTH, 2);

        String actualMessage = textMakerFr.playerScoredHuntingTrap(RED, Points.forMeadow(animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER)), animals);
        String expectedMessage = "Dalia a remporté %d points en plaçant la fosse à pieux dans un pré dans lequel elle est entourée de %s mammouths, %s aurochs et %s cerfs.".formatted(Points.forMeadow(animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER)), animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER));
        String actualMessage2 = textMakerFr.playerScoredHuntingTrap(GREEN, Points.forMeadow(animals2.get(MAMMOTH), 0, 0), animals2);
        String expectedMessage2 = "Bachir a remporté 6 points en plaçant la fosse à pieux dans un pré dans lequel elle est entourée de 2 mammouths.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
    }

    @Test
    void playerScoredLogboatWorks() {

        TextMakerFr textMakerFr = new TextMakerFr(players());

        String actualMessage = textMakerFr.playerScoredLogboat(RED, Points.forLogboat(2), 2);
        String expectedMessage = "Dalia a remporté 4 points en plaçant la pirogue dans un réseau hydrographique contenant 2 lacs.";
        String actualMessage2 = textMakerFr.playerScoredLogboat(GREEN, Points.forLogboat(1), 1);
        String expectedMessage2 = "Bachir a remporté 2 points en plaçant la pirogue dans un réseau hydrographique contenant 1 lac.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
    }

    @Test
    void playersScoredMeadowWorks() {
        Random random = new Random();
        TextMakerFr textMakerFr = new TextMakerFr(players());
        TreeMap<Animal.Kind, Integer> animals = new TreeMap<>();
        for (int i = 0; i < Animal.Kind.values().length; i++) {
            animals.put(Animal.Kind.values()[i], random.nextInt(2, 4));
        }

        TreeMap<Animal.Kind, Integer> animals2 = new TreeMap<>();
        animals2.put(MAMMOTH, 2);

        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);
        Set<PlayerColor> scorers2 = Set.of(GREEN);

        String actualMessage = textMakerFr.playersScoredMeadow(scorers, Points.forMeadow(animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER)), animals);
        String expectedMessage = "Dalia, Claude et Alice ont remporté %d points en tant qu'occupant·e·s majoritaires d'un pré contenant %d mammouths, %d aurochs et %d cerfs.".formatted(Points.forMeadow(animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER)), animals.get(MAMMOTH), animals.get(AUROCHS), animals.get(DEER));
        String actualMessage2 = textMakerFr.playersScoredMeadow(scorers2, Points.forMeadow(animals2.get(MAMMOTH), 0, 0), animals2);
        String expectedMessage2 = "Bachir a remporté 6 points en tant qu'occupant·e majoritaire d'un pré contenant 2 mammouths.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
    }

    @Test
    void playersScoredRiverSystemWorks() {

        TextMakerFr textMakerFr = new TextMakerFr(players());

        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);
        Set<PlayerColor> scorers2 = Set.of(GREEN);
        Set<PlayerColor> scorers3 = Set.of(YELLOW, BLUE);

        String actualMessage = textMakerFr.playersScoredRiverSystem(scorers, Points.forRiverSystem(2), 2);
        String expectedMessage = "Dalia, Claude et Alice ont remporté 2 points en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant 2 poissons.";
        String actualMessage2 = textMakerFr.playersScoredRiverSystem(scorers2, Points.forRiverSystem(1), 1);
        String expectedMessage2 = "Bachir a remporté 1 point en tant qu'occupant·e majoritaire d'un réseau hydrographique contenant 1 poisson.";
        String actualMessage3 = textMakerFr.playersScoredRiverSystem(scorers3, Points.forRiverSystem(1), 1);
        String expectedMessage3 = "Claude et Alice ont remporté 1 point en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant 1 poisson.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
        assertEquals(expectedMessage3, actualMessage3);
    }

    @Test
    void playersScoredPitTrapWork() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        TreeMap<Animal.Kind, Integer> animals = new TreeMap<>();
        animals.put(MAMMOTH, 1);
        animals.put(DEER, 3);
        animals.put(AUROCHS, 2);
        String actual = textMakerFr.playersScoredPitTrap(Set.of(PlayerColor.GREEN), 10, animals);
        String expected = "Bachir a remporté 10 points en tant qu'occupant·e majoritaire d'un pré contenant la grande fosse à pieux entourée de 1 mammouth, 2 aurochs et 3 cerfs.";
        assertEquals(expected, actual);
    }

    @Test
    void playersScoredRaftWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());

        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);
        Set<PlayerColor> scorers2 = Set.of(GREEN);
        Set<PlayerColor> scorers3 = Set.of(YELLOW, BLUE);

        String actualMessage = textMakerFr.playersScoredRaft(scorers, Points.forRaft(2), 2);
        String expectedMessage = "Dalia, Claude et Alice ont remporté 2 points en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant le radeau et 2 lacs.";
        String actualMessage2 = textMakerFr.playersScoredRaft(scorers2, Points.forRaft(1), 1);
        String expectedMessage2 = "Bachir a remporté 1 point en tant qu'occupant·e majoritaire d'un réseau hydrographique contenant le radeau et 1 lac.";
        String actualMessage3 = textMakerFr.playersScoredRaft(scorers3, Points.forRaft(1), 1);
        String expectedMessage3 = "Claude et Alice ont remporté 1 point en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant le radeau et 1 lac.";
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
        assertEquals(expectedMessage3, actualMessage3);
    }

    @Test
    void playersWonWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());

        Set<PlayerColor> scorers = Set.of(BLUE, YELLOW, RED);

        Set<PlayerColor> scorers2 = Set.of(GREEN);

        String actualMessage = textMakerFr.playersWon(scorers, 10);
        String expectedMessage = "Dalia, Claude et Alice ont remporté la partie avec 10 points !";

        String actualMessage2 = textMakerFr.playersWon(scorers2, 7);
        String expectedMessage2 = "Bachir a remporté la partie avec 7 points !";

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);
    }

    //Inutile les deux dernières méthodes, mais je fais quand même flemme de te demander.
    @Test
    void clickToOccupyWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        String actualMessage = textMakerFr.clickToOccupy();
        String expectedMessage = "Cliquez sur le pion ou la hutte que vous désirez placer, ou ici pour ne pas en placer.";
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void clickToUnoccupyWorks() {
        TextMakerFr textMakerFr = new TextMakerFr(players());
        String actualMessage = textMakerFr.clickToUnoccupy();
        String expectedMessage = "Cliquez sur le pion que vous désirez reprendre, ou ici pour ne pas en reprendre.";
        assertEquals(expectedMessage, actualMessage);
    }
}