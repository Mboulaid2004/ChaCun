package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StringTemplate.STR;

public final class TextMakerFr implements TextMaker{
    private final Map<PlayerColor,String> playerNames;

    public TextMakerFr(Map<PlayerColor, String> playerNames) {
        this.playerNames = Map.copyOf(playerNames);
    }

    @Override
    public String playerName(PlayerColor playerColor) {
        Preconditions.checkArgument(playerColor != null);
        return playerNames.get(playerColor);
    }

    @Override
    public String points(int points) {
        return STR."\{points} point\{addS(points)}";
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return STR."\{playerName(player)} a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.";
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> scorersList  = sortPlayers(scorers);
        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));

        if(mushroomGroupCount == 0){
            return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'une forêt " +
            STR."composée de \{tileCount} tuile\{addS(tileCount)}.";
        }

        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'une forêt " +
                STR."composée de \{tileCount} tuile\{addS(tileCount)} et de \{mushroomGroupCount} groupe\{addS(mushroomGroupCount)} de champignons.";
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> scorersList  = sortPlayers(scorers);
        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));


        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'une " +
        STR."rivière composée de \{tileCount} tuile\{addS(tileCount)}\{contains(fishCount,"poisson")}";
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, TreeMap<Animal.Kind, Integer> animals) {//dire pq on a mis des treempaps
        Preconditions.checkArgument(!animals.isEmpty());
        Preconditions.checkArgument(scorer != null);

        List<String> animalsList = animalsToFrench(animals);
        String allAnimals = animalsList.stream().limit(animalsList.size() -1).collect(Collectors.joining(", "));

        if(animals.size() == 1)
            return STR."\{playerName(scorer)} a remporté \{points} point\{addS(points)} en plaçant la fosse à pieux dans un pré dans lequel elle est " +
            STR."entourée de \{animalsList.getFirst()}.";


        return STR."\{playerName(scorer)} a remporté \{points} point\{addS(points)} en plaçant la fosse à pieux dans un pré dans lequel elle est entourée " +
        STR."de \{allAnimals} et \{animalsList.getLast()}.";

    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        Preconditions.checkArgument(scorer != null);

        return STR."\{playerName(scorer)} a remporté \{points} point\{addS(points)} en plaçant la pirogue dans un réseau hydrographique " +
                STR."contenant \{lakeCount} lac\{addS(lakeCount)}.";
    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, TreeMap<Animal.Kind, Integer> animals) {
        Preconditions.checkArgument(!animals.isEmpty());
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> animalsList = animalsToFrench(animals);
        String allAnimals = animalsList.stream()
                .limit(animalsList.size() -1).collect(Collectors.joining(", "));

        List<String> scorersList  = sortPlayers(scorers);
        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));

        if(animals.size() == 1)
            return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'un pré " +
            STR."contenant \{animalsList.getFirst()}.";


        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} " +
        STR."d'un pré contenant \{allAnimals} et \{animalsList.getLast()}.";
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> scorersList  = sortPlayers(scorers);
        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));

        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'un réseau " +
                STR."hydrographique contenant \{fishCount} poisson\{addS(fishCount)}.";
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, TreeMap<Animal.Kind, Integer> animals) {
        Preconditions.checkArgument(!animals.isEmpty());
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> animalsList = animalsToFrench(animals);
        String allAnimals = animalsList.stream().limit(animalsList.size() -1).collect(Collectors.joining(", "));

        List<String> scorersList = sortPlayers(scorers);

        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));

        if(animals.size() == 1)
            return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} " +
                    STR." d'un pré contenant la grande fosse à pieux entourée de \{animalsList.getFirst()}.";


        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} "
                + STR."d'un pré contenant la grande fosse à pieux entourée de \{allAnimals} et \{animalsList.getLast()}.";

    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        Preconditions.checkArgument(!scorers.isEmpty());

        List<String> scorersList  = sortPlayers(scorers);
        String allScorers = scorersList.stream().limit(scorersList.size() -1).collect(Collectors.joining(", "));

        return STR."\{etOn(scorersList,allScorers)} remporté \{points} point\{addS(points)} en tant qu'\{occupantS(scorersList,allScorers)} d'un " +
        STR."réseau hydrographique contenant le radeau et \{lakeCount} lac\{addS(lakeCount)}.";
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        List<String> winnersList  = sortPlayers(winners);

        if(winners.size() == 1){
            return STR."\{winnersList.getFirst()} a remporté la partie avec \{points} point\{addS(points)} !";
        }

        if(!winners.isEmpty()){
            String win = winnersList.stream().limit(winnersList.size() -1).collect(Collectors.joining(", "));

            return STR."\{win} et \{winnersList.getLast()} ont remporté la partie avec \{points} point\{addS(points)} !";
        }

         return "La partie s'est terminée sans aucun gagnant.";
    }

    @Override
    public String clickToOccupy() {
        return STR."Cliquez sur le pion ou la hutte que vous désirez placer, ou ici pour ne pas en placer.";
    }

    @Override
    public String clickToUnoccupy() {
        return STR."Cliquez sur le pion que vous désirez reprendre, ou ici pour ne pas en reprendre.";
    }


    private List<String> animalsToFrench(TreeMap<Animal.Kind,Integer> animals){//traduit les animaux de l'anglais au francais
        Map<Animal.Kind, String> frenchAnimalNames = new HashMap<>();
        frenchAnimalNames.put(Animal.Kind.MAMMOTH, "mammouth");
        frenchAnimalNames.put(Animal.Kind.AUROCHS, "auroch");
        frenchAnimalNames.put(Animal.Kind.DEER, "cerf");

        TreeMap<Animal.Kind,Integer> newAnimals = new TreeMap<>();
        for(Map.Entry<Animal.Kind,Integer> entry : animals.entrySet()){
            if(entry.getKey() != Animal.Kind.TIGER) newAnimals.put(entry.getKey(),entry.getValue());
        }

        return newAnimals.entrySet().stream().map(entry -> STR."\{entry.getValue()} \{frenchAnimalNames.get(entry.getKey())}\{addS(entry.getValue())}")
                .toList();
    }

    private String addS(int count){//détermine le pluriel ou le singulier
        if(count > 1) return "s";
        return "";
    }

    private List<String> sortPlayers(Set<PlayerColor> players){//crée une list triée avec les prénoms des joueurs
        List<PlayerColor> playerColors = new ArrayList<>(players);
        playerColors.sort(Comparator.comparingInt(PlayerColor::ordinal));

        return new ArrayList<>(playerColors.stream().map(this::playerName).toList());
    }

    private String contains(int animalCount, String animal){
        if( animalCount > 0) return STR." et contenant \{animalCount} \{animal}\{addS(animalCount)}.";
        return ".";
    }

    private String etOn(List<String> list, String s){
        if(list.size() == 1) return STR."\{list.getFirst()} a";
        return STR."\{s} et \{list.getLast()} ont";
    }

    private String occupantS(List<String> list,String s){//gere le pluriel ou singulier
        if(Objects.equals(etOn(list, s), STR."\{list.getFirst()} a")) return "occupant·e majoritaire";
        return "occupant·e·s majoritaires";
    }

}
