package ch.epfl.chacun;




import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;




/**
 * L'enregistrement GameState représente l'état complet d'une partie de ChaCuN.
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public record GameState(List<PlayerColor> players, TileDecks tileDecks, Tile tileToPlace, Board board, Action nextAction, MessageBoard messageBoard) {




    /**
     * Enumération des actions possibles dans le jeu.
     */
    public enum Action{
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME;
    }




    /**
     * Constructeur de la classe GameState.
     * @param players La liste de tous les joueurs de la partie, dans l'ordre dans lequel ils doivent jouer.
     * @param tileDecks Les trois tas des tuiles restantes.
     * @param tileToPlace L'éventuelle tuile à placer, qui à été prise du sommet du tas des tuiles normales ou du tas des tuiles menhir, et qui peut être null si aucune tuile n'est à placer actuellement.
     * @param board Le plateau de jeu.
     * @param nextAction La prochaine action à effectuer.
     * @param messageBoard Le tableau d'affichage contenant les messages générés jusqu'à présent dans la partie.
     * @throws IllegalArgumentException Si les joueurs sont moins de 2 ou si l'action suivante est une pose de tuile sans tuile à placer, ou si certains éléments sont nuls.
     */
    public GameState{
        Preconditions.checkArgument(players.size() >= 2);
        players = List.copyOf(players);
        Objects.requireNonNull(tileDecks);
        Preconditions.checkArgument(nextAction.equals(Action.PLACE_TILE ) ^ tileToPlace == null);




        Objects.requireNonNull(board);
        Objects.requireNonNull(messageBoard);
    }




    /**
     * Crée l'état initial du jeu.
     *
     * @param players    Liste des couleurs des joueurs.
     * @param tileDecks  Paquets de tuiles disponibles.
     * @param textMaker  Générateur de texte.
     * @return L'état initial du jeu.
     */
    public static GameState initial(List<PlayerColor> players, TileDecks tileDecks, TextMaker textMaker){
        return new GameState(players,tileDecks,null,Board.EMPTY,Action.START_GAME, new MessageBoard(textMaker,List.of()));
    }




    /**
     * Retourne le joueur actuel.
     *
     * @return Le joueur actuel, ou null si le jeu n'a pas encore commencé ou s'est terminé.
     */
    public PlayerColor currentPlayer(){
        if(nextAction.equals(Action.START_GAME) || nextAction.equals(Action.END_GAME)){
            return null;
        }
        return players.getFirst();
    }




    /**
     * Calcule le nombre d'occupants libres d'un certain type pour un joueur donné.
     *
     * @param player Couleur du joueur.
     * @param kind   Type d'occupant.
     * @return Le nombre d'occupants libres.
     */
    public int freeOccupantsCount(PlayerColor player, Occupant.Kind kind){
        return Occupant.occupantsCount(kind) - board.occupantCount(player, kind);
    }




    /**
     * Calcule les occupants potentiels de la dernière tuile placée.
     *
     * @return L'ensemble des occupants potentiels de la dernière tuile placée.
     * @throws IllegalArgumentException Si le plateau est vide.
     */
    public Set<Occupant> lastTilePotentialOccupants(){//retourne l'ensemble des occupants potentiels de la DERNIERE placedTile
        Preconditions.checkArgument(board != Board.EMPTY);
        Set<Occupant> lastTilePotentialOccupants = board.lastPlacedTile().potentialOccupants();

        for(Occupant potentialOcpnt : board.lastPlacedTile().potentialOccupants()){
            switch(board.lastPlacedTile().zoneWithId(potentialOcpnt.zoneId())){
                case Zone.Meadow meadow
                        when (freeOccupantsCount(currentPlayer(),potentialOcpnt.kind()) == 0 || board.meadowArea(meadow).isOccupied())->
                        lastTilePotentialOccupants.remove(potentialOcpnt);

                case Zone.Forest forest
                        when (freeOccupantsCount(currentPlayer(),potentialOcpnt.kind()) == 0 || board.forestArea(forest).isOccupied())->
                        lastTilePotentialOccupants.remove(potentialOcpnt);

                case Zone.River river
                        when potentialOcpnt.kind().equals(Occupant.Kind.PAWN) && (freeOccupantsCount(currentPlayer(),potentialOcpnt.kind()) == 0 ||
                        board.riverArea(river).isOccupied()) ->
                        lastTilePotentialOccupants.remove(potentialOcpnt);

                case Zone.River river
                        when (freeOccupantsCount(currentPlayer(),potentialOcpnt.kind()) == 0 || board.riverSystemArea(river).isOccupied()) &&
                        potentialOcpnt.kind().equals(Occupant.Kind.HUT) ->
                        lastTilePotentialOccupants.remove(potentialOcpnt);

                case Zone.Lake lake
                        when freeOccupantsCount(currentPlayer(),potentialOcpnt.kind()) == 0 || board.riverSystemArea(lake).isOccupied() ->
                        lastTilePotentialOccupants.remove(potentialOcpnt);

                default -> {}
            }
        }
        return lastTilePotentialOccupants;
    }

    /**
     * Ajoute la tuile de départ au plateau de jeu.
     *
     * @return L'état de jeu après avoir placé la tuile de départ.
     * @throws IllegalArgumentException Si la prochaine action n'est pas {@code START_GAME}.
     */
    public GameState withStartingTilePlaced() {
        if (!nextAction.equals(Action.START_GAME)) {
            throw new IllegalArgumentException("Game must begin before placing the starting tile.");
        }

        TileDecks decksWithDrawnTiles = new TileDecks(tileDecks.startTiles(), tileDecks.normalTiles(), tileDecks.menhirTiles())
                .withTopTileDrawn(Tile.Kind.START)
                .withTopTileDrawn(Tile.Kind.NORMAL);
        Tile nextNormalTile = tileDecks.topTile(Tile.Kind.NORMAL);
        PlacedTile startingPlacedTile = new PlacedTile(tileDecks.topTile(Tile.Kind.START), null, Rotation.NONE, Pos.ORIGIN, null);
        Board boardWithStartingTile = board.withNewTile(startingPlacedTile);

        return new GameState(players, decksWithDrawnTiles, nextNormalTile, boardWithStartingTile, Action.PLACE_TILE, messageBoard);
    }

    /**
     * Gère la transition à partir de l'action {@code PLACE_TILE} en ajoutant la tuile donnée au plateau de jeu.
     * Cette méthode attribue également les points correspondant à la pose de la pirogue ou de la fosse à pieux le cas échéant,
     * et détermine la prochaine action à effectuer.
     *
     * @param tile La tuile à placer sur le plateau.
     * @return L'état de jeu mis à jour après avoir placé la tuile donnée.
     * @throws IllegalArgumentException Si la prochaine action n'est pas {@code PLACE_TILE}, ou si la tuile passée est déjà occupée.
     */
    public GameState withPlacedTile(PlacedTile tile) {
        Preconditions.checkArgument(this.nextAction == Action.PLACE_TILE);
        Preconditions.checkArgument(tile.occupant() == null);

        Board updatedBoard = board.withNewTile(tile);
        MessageBoard nextMessage = messageBoard;
        Zone specialZone = tile.specialPowerZone();

        if (specialZone != null) {
            switch (specialZone){
                case Zone.Lake lake when lake.specialPower() == Zone.SpecialPower.LOGBOAT ->
                        nextMessage = nextMessage.withScoredLogboat(currentPlayer(),updatedBoard.riverSystemArea(lake));

                case Zone.Meadow meadow when meadow.specialPower() == Zone.SpecialPower.HUNTING_TRAP ->{
                        Set<Animal> newCancelledAnimals = Area.animals(updatedBoard.adjacentMeadow(tile.pos(),meadow),updatedBoard.cancelledAnimals());

                        nextMessage = nextMessage.withScoredHuntingTrap(currentPlayer(),updatedBoard.adjacentMeadow(tile.pos(),meadow),updatedBoard.cancelledAnimals());
                        updatedBoard = updatedBoard.withMoreCancelledAnimals(updateCancelledAnimalsForDeer(newCancelledAnimals));
                }

                case Zone.Meadow meadow when meadow.specialPower() == Zone.SpecialPower.SHAMAN -> {
                    if(freeOccupantsCount(currentPlayer(), Occupant.Kind.PAWN) == Occupant.occupantsCount(Occupant.Kind.PAWN)){
                        return new GameState(players, tileDecks,
                                null, updatedBoard, Action.OCCUPY_TILE, nextMessage).withTurnFinishedIfOccupationImpossible();
                    }
                    else{
                        return new GameState(players, tileDecks,
                                null, updatedBoard, Action.RETAKE_PAWN, nextMessage);
                    }
                }


                default -> {
                    return new GameState(players, tileDecks, null, updatedBoard, Action.OCCUPY_TILE, nextMessage).withTurnFinishedIfOccupationImpossible();
                }
            }
        }


        return new GameState(players, tileDecks, null, updatedBoard, Action.OCCUPY_TILE, nextMessage).withTurnFinishedIfOccupationImpossible();
    }




    /**
     * Supprime un occupant du plateau de jeu.
     *
     * @param occupant Occupant à retirer.
     * @return L'état de jeu après avoir retiré l'occupant.
     * @throws IllegalArgumentException Si la prochaine action n'est pas {@code RETAKE_PAWN} et si l'occupant est nul ou n'est pas un pion.
     */
    public GameState withOccupantRemoved(Occupant occupant){
        Preconditions.checkArgument(nextAction == Action.RETAKE_PAWN && (occupant == null || occupant.kind() == Occupant.Kind.PAWN));


        if(occupant == null){
            return new GameState(players,tileDecks,null,board,Action.OCCUPY_TILE,messageBoard).withTurnFinishedIfOccupationImpossible();
        }


        return new GameState(players,tileDecks,null,board.withoutOccupant(occupant),Action.OCCUPY_TILE,messageBoard);
    }


    /**
     * Ajoute un nouvel occupant au plateau de jeu.
     *
     * @param occupant Nouvel occupant à ajouter.
     * @return L'état de jeu après avoir ajouté l'occupant.
     * @throws IllegalArgumentException Si la prochaine action n'est pas {@code OCCUPY_TILE}.
     */
    public GameState withNewOccupant(Occupant occupant){
        Preconditions.checkArgument(nextAction.equals(Action.OCCUPY_TILE));


        if (occupant == null) {
            return this.withTurnFinished();
        } else {
            //PlacedTile lastPlacedTile = board.lastPlacedTile();
            //Zone zone = lastPlacedTile.zoneWithId(occupant.zoneId());
            Board updatedBoard = board.withOccupant(occupant);
            return new GameState(players, tileDecks, null, updatedBoard, nextAction, messageBoard).withTurnFinished();
        }
    }


    private GameState withTurnFinished() {
        // Mise à jour des forêts et des rivières fermées par la dernière tuile placée
        Set<Area<Zone.Forest>> closedForests = new HashSet<>(board.forestsClosedByLastTile());
        Set<Area<Zone.River>> closedRivers = new HashSet<>(board.riversClosedByLastTile());
        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board;
        TileDecks newTileDecks = tileDecks;
        List<PlayerColor> newPlayers = new ArrayList<>(players);
        Action newAction;
        Tile newTile = null;
        Tile.Kind kind;


        // Suppression des chasseurs ou pêcheurs dans les forêts et les rivières fermées
        newBoard = newBoard.withoutGatherersOrFishersIn(closedForests, closedRivers);


        // Attribution des points pour les forêts et rivières fermées
        for (Area<Zone.Forest> forestArea : closedForests) {
            newMessageBoard = newMessageBoard.withScoredForest(forestArea);
        }


        for (Area<Zone.River> riverArea : closedRivers) {
            newMessageBoard = newMessageBoard.withScoredRiver(riverArea);
        }


        // Vérification de la présence de menhirs dans les forêts fermées
        boolean hasAnyMenhir = false;
        boolean isLastTileNormal = board.lastPlacedTile().kind() == Tile.Kind.NORMAL;



        for (Area<Zone.Forest> forestArea : closedForests) {
            if (Area.hasMenhir(forestArea) && isLastTileNormal) {
                if (newTileDecks.deckSize(Tile.Kind.MENHIR) > 0) {
                    hasAnyMenhir = true;
                    newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), forestArea);
                }
            }
        }

        // Gestion des menhirs
        if (hasAnyMenhir) {
            if(newTileDecks.deckSize(Tile.Kind.MENHIR) > 0){
                newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, board::couldPlaceTile);
            }
            newTile = newTileDecks.topTile(Tile.Kind.MENHIR);


            // Si aucun menhir n'est disponible, continuer avec les tuiles normales
            if (newTile == null) {
                if(newTileDecks.deckSize(Tile.Kind.NORMAL) > 0){
                    newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.NORMAL, board::couldPlaceTile);
                }
                newTile = newTileDecks.topTile(Tile.Kind.NORMAL);
                kind = Tile.Kind.NORMAL;
                newAction = Action.PLACE_TILE;
                Collections.rotate(newPlayers, -1);
            }
            else {
                kind = Tile.Kind.MENHIR;
                newAction = Action.PLACE_TILE;}


        } else {// Si aucun menhir n'est présent, continuer avec les tuiles normales
            if(newTileDecks.deckSize(Tile.Kind.NORMAL) > 0){
                newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.NORMAL, board::couldPlaceTile);
                newTile = newTileDecks.topTile(Tile.Kind.NORMAL);
            }


            kind = Tile.Kind.NORMAL;
            newAction = Action.PLACE_TILE;
            Collections.rotate(newPlayers, -1);
        }


        // Si aucune tuile n'est disponible et qu'il n'y a plus de tuiles normales dans le paquet, terminer le jeu
        if (newTile == null && tileDecks.deckSize(Tile.Kind.NORMAL) == 0) {
            newAction = Action.END_GAME;
            return new GameState(newPlayers, newTileDecks, null, newBoard, newAction, newMessageBoard).withFinalPointsCounted();
        }


        // Retirer la tuile du dessus du paquet de tuiles et créer un nouvel état de jeu avec la tuile placée
        newTileDecks = newTileDecks.withTopTileDrawn(kind);
        return new GameState(newPlayers, newTileDecks, newTile, newBoard, newAction, newMessageBoard);
    }


    private GameState withTurnFinishedIfOccupationImpossible(){
        GameState gameState = new GameState(players,tileDecks,null,board,Action.OCCUPY_TILE,messageBoard);
        return lastTilePotentialOccupants().isEmpty() ? withTurnFinished() : gameState;
    }

    private GameState withFinalPointsCounted() {
        Set<Animal> cancelledAnimalsUpdated = new HashSet<>(board.cancelledAnimals());
        Board newBoard = board;
        MessageBoard updatedMessageBoard = messageBoard;


        // Parcours de toutes les zones de prairie et vérification des pouvoirs spéciaux
        for (Area<Zone.Meadow> meadow : newBoard.meadowAreas()) {
            if(meadow.zoneWithSpecialPower(Zone.SpecialPower.WILD_FIRE) != null){

                if(meadow.zoneWithSpecialPower(Zone.SpecialPower.PIT_TRAP) != null){
                    updatedMessageBoard = updatedMessageBoard.withScoredMeadow(meadow,cancelledAnimalsUpdated);
                    updatedMessageBoard = updatedMessageBoard.withScoredPitTrap(meadow,cancelledAnimalsUpdated);
                }

                updatedMessageBoard = updatedMessageBoard.withScoredMeadow(meadow,cancelledAnimalsUpdated);

            }else {
                // Attribuer les potentiels points pour la fosse a pieux dans la prairie et les cerfs devorés
                if(meadow.zoneWithSpecialPower(Zone.SpecialPower.PIT_TRAP) != null){
                    newBoard = newBoard.withMoreCancelledAnimals(updateCancelledAnimalsForDeer(cancelledAnimalsUpdated));
                    updatedMessageBoard = updatedMessageBoard.withScoredPitTrap(meadow,newBoard.cancelledAnimals());
                }

                newBoard = newBoard.withMoreCancelledAnimals(updateCancelledAnimalsForDeer(cancelledAnimalsUpdated));
                updatedMessageBoard = updatedMessageBoard.withScoredMeadow(meadow,newBoard.cancelledAnimals());
            }
        }


        // Attribuer les potentiels points pour chaque réseau hydrographique occupé par des huttes
        for (Area<Zone.Water> riverSystem : board.riverSystemAreas()) {
            if(riverSystem.zoneWithSpecialPower(Zone.SpecialPower.RAFT) != null){
                updatedMessageBoard = updatedMessageBoard.withScoredRaft(riverSystem);
            }

            updatedMessageBoard = updatedMessageBoard.withScoredRiverSystem(riverSystem);
        }


        // Déterminer le(s) vainqueur(s) en fonction des points accumulés
        Map<PlayerColor, Integer> finalScores = updatedMessageBoard.points();
        int highestScore = finalScores.values().stream().max(Integer::compare).orElse(0);


        Set<PlayerColor> winners = finalScores.entrySet().stream()
                .filter(entry -> entry.getValue() == highestScore)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());


        updatedMessageBoard = updatedMessageBoard.withWinners(winners, highestScore);



        return new GameState(players, tileDecks, null, newBoard, Action.END_GAME, updatedMessageBoard);
    }



    private Set<Animal> updateCancelledAnimalsForDeer(Set<Animal> initiallyCancelledAnimals) {
        Set<Animal> updatedCancelledAnimals = new HashSet<>(initiallyCancelledAnimals);


        for (Area<Zone.Meadow> meadowArea : board.meadowAreas()) {
            boolean hasFire = meadowArea.zones().stream()
                    .anyMatch(zone -> zone.specialPower() == Zone.SpecialPower.WILD_FIRE);


            boolean hasHuntingTrap = meadowArea.zones().stream()
                    .anyMatch(zone -> zone.specialPower() == Zone.SpecialPower.HUNTING_TRAP);


            Set<Animal> totalDeers = meadowArea.zones().stream()
                    .flatMap(zone -> zone.animals().stream())
                    .filter(animal -> animal.kind() == Animal.Kind.DEER && !updatedCancelledAnimals.contains(animal)).collect(Collectors.toSet());


            int totalDeersCount = totalDeers.size();


            Set<Animal> totalSmilodons = meadowArea.zones().stream()
                    .flatMap(zone -> zone.animals().stream())
                    .filter(animal -> animal.kind() == Animal.Kind.TIGER)
                    .collect(Collectors.toSet());


            int totalSmilodonsCount = totalSmilodons.size();


            if (!hasFire && totalSmilodonsCount > 0) {
                int deerToCancel = Math.min(totalDeersCount, totalSmilodonsCount);
                for (Zone.Meadow meadow : meadowArea.zones()) {
                    for (Animal animal : meadow.animals()) {
                        if (animal.kind() == Animal.Kind.DEER && deerToCancel > 0) {
                            updatedCancelledAnimals.add(animal);
                            deerToCancel--;
                        }
                    }
                }
            }

            if(hasHuntingTrap && totalSmilodonsCount > 0){
                for (Zone.Meadow meadow : meadowArea.zones()) {
                    updatedCancelledAnimals.addAll(meadow.animals());
                }
                int deerToCancel = Math.min(totalDeersCount, totalSmilodonsCount);
                for (Zone.Meadow meadow : meadowArea.zones()) {
                    for (Animal animal : meadow.animals()) {
                        if (animal.kind() == Animal.Kind.DEER && deerToCancel > 0) {
                            updatedCancelledAnimals.add(animal);
                            deerToCancel--;
                        }
                    }
                }
            }

        }
        return updatedCancelledAnimals;
    }

}

