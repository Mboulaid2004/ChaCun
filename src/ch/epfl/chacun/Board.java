package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plateau de jeu.
 *
 * @author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public class Board {
    private final PlacedTile[] placedTiles;
    private final int[] indexes;
    private final ZonePartitions zonePartitions;
    private final Set<Animal> cancelledAnimals;

    private Board(PlacedTile[] placedTiles,int[] indexes, ZonePartitions zonePartitions, Set<Animal> cancelledAnimals){
        this.placedTiles = placedTiles;
        this.indexes = indexes;
        this.zonePartitions = zonePartitions;
        this.cancelledAnimals =cancelledAnimals;
    }

    /**
     * la «portée» du plateau, qui est le nombre de cases qui séparent la case centrale de l'un des bords du plateau.
     */
    public static final int REACH = 12;

    /**
     * la «longueur» du plateau, qui est le nombre de cases qui séparent la case centrale de l'un des bords du plateau.
     */
    public static final int LENGTH = REACH*2 + 1;
    public static final int SIZE = LENGTH*LENGTH;
    public static final Board EMPTY = new Board(new PlacedTile[SIZE], new int[0], ZonePartitions.EMPTY, new HashSet<>());

    /**
     * Retourne la tuile à la position spécifiée.
     *
     * @param pos La position à vérifier.
     * @return La tuile à la position spécifiée, ou null si la position est hors de portée.
     */
    public PlacedTile tileAt(Pos pos){
        if(pos.x() > REACH || pos.x() < -REACH || pos.y() > REACH || pos.y() < -REACH){
            return null;
        }
        int index = (pos.y() + REACH)*LENGTH + (pos.x() + REACH);

        return placedTiles[index];
    }

    /**
     * Retourne la tuile avec l'identifiant spécifié.
     *
     * @param tileId L'identifiant de la tuile à rechercher.
     * @return La tuile avec l'identidiiant spécifié.
     * @throws IllegalArgumentException si aucune tuile avec l'ID spécifié n'est trouvée.
     */
    public PlacedTile tileWithId(int tileId){
        for(int i : indexes){
            if(placedTiles[i].id() == tileId && placedTiles[i] != null){ return placedTiles[i]; }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Retourne l'ensemble des animaux annulés sur le plateau.
     *
     * @return L'ensemble des animaux annulés sur le plateau.
     */
    public Set<Animal> cancelledAnimals(){
        return Set.copyOf(cancelledAnimals);
    }

    /**
     * Retourne l'ensemble des occupants présents sur le plateau.
     *
     * @return L'ensemble des occupants présents sur le plateau.
     */
    public Set<Occupant> occupants(){
        Set<Occupant> o = new HashSet<>();
        for(PlacedTile p : placedTiles){
            if(p != null && p.occupant() != null){
                o.add(p.occupant());
            }
        }
        return o;
    }

    /**
     * Retourne l'aire forêt contenant la zone spécifiée.
     *
     * @param forest La zone forestière dont on veut obtenir l'aire.
     * @return La zone forestière contenant la zone spécifiée.
     */
    public Area<Zone.Forest> forestArea(Zone.Forest forest){
        return zonePartitions.forests().areaContaining(forest);
    }

    /**
     * Retourne l'aire prairie contenant la zone spécifiée.
     *
     * @param meadow La zone prairie dont on veut obtenir l'aire.
     * @return La zone prairie contenant la zone spécifiée.
     */
    public Area<Zone.Meadow> meadowArea(Zone.Meadow meadow){
        return zonePartitions.meadows().areaContaining(meadow);
    }

    /**
     * Retourne l'aire rivière contenant la zone spécifiée.
     *
     * @param river La zone rivière dont on veut obtenir l'aire.
     * @return L'aire rivière contenant la zone spécifiée.
     */
    public Area<Zone.River> riverArea(Zone.River river){
        return zonePartitions.rivers().areaContaining(river);
    }

    /**
     * Retourne l'aire du système hydrographique contenant la zone spécifiée.
     *
     * @param water La zone de système hydrographique dont on veut obtenir l'aire.
     * @return L'aire du système hydrographique contenant la zone spécifiée.
     */
    public Area<Zone.Water> riverSystemArea(Zone.Water water){ return zonePartitions.riverSystems().areaContaining(water);}

    /**
     * Retourne l'ensemble des aires de prairie sur le plateau.
     *
     * @return L'ensemble des aires de prairie sur le plateau.
     */
    public Set<Area<Zone.Meadow>> meadowAreas(){
        return zonePartitions.meadows().areas();
    }

    /**
     * Retourne l'ensemble des aires de systèmes hydrographiques sur le plateau.
     *
     * @return L'ensemble des aires de systèmes hydrographiques sur le plateau.
     */
    public Set<Area<Zone.Water>> riverSystemAreas(){
        return zonePartitions.riverSystems().areas();
    }

    /**
     * Retourne l'aire de prairie adjacente à la zone de la fosse à pieux.
     *
     * @param pos La position de la fosse à pieux.
     * @param meadowZone La zone de la fosse à pieux.
     * @return La zone de prairie adjacente à la position et à la zone spécifiées.
     */
    public Area<Zone.Meadow> adjacentMeadow(Pos pos, Zone.Meadow meadowZone){
        Area<Zone.Meadow> meadowsArea = meadowArea(meadowZone);
        Set<Zone.Meadow> neighborZones = new HashSet<>();

        for(int i=-1; i<2; i++){
            for(int j = -1; j<2; j++){
                Pos newpos = pos.translated(i,j);
                PlacedTile placedTile = tileAt(newpos);

                if(placedTile != null){
                    neighborZones.addAll(placedTile.meadowZones());
                }
            }
        }

        neighborZones.retainAll(meadowsArea.zones());

        return new Area<>(neighborZones,meadowsArea.occupants(),0);
    }


    /**
     * Compte le nombre d'occupants du plateau d'un certain type pour un joueur donné.
     *
     * @param player Le joueur pour lequel compter les occupants.
     * @param occupantKind Le type d'occupant à compter.
     * @return Le nombre d'occupants du type spécifié pour le joueur spécifié.
     */
    public int occupantCount(PlayerColor player, Occupant.Kind occupantKind){
        int count = 0;

        for(int i : indexes){
            if(placedTiles[i].occupant() != null) {
                if (placedTiles[i].placer().equals(player) && placedTiles[i].occupant().kind().equals(occupantKind)) {
                    count += 1;
                }
            }
        }
        return count;
    }


    private boolean LimitOnBoardIsRespected(Pos pos) {
        return(Math.abs(pos.x()) <= REACH &&  Math.abs(pos.y()) <= REACH);
    }

    /**
     * Retourne l'ensemble des positions où un nouveau tuile peut être insérée.
     *
     * @return L'ensemble des positions d'insertion disponibles.
     */
    public Set<Pos> insertionPositions(){
        Set<Pos> insertpos = new HashSet<>();

        for(int i : indexes){
            for(Direction d : Direction.ALL){
                if(tileAt(placedTiles[i].pos().neighbor(d)) == null && LimitOnBoardIsRespected(placedTiles[i].pos().neighbor(d))){
                    insertpos.add(placedTiles[i].pos().neighbor(d));
                }
            }
        }
        return insertpos;
    }

    /**
     * Retourne la dernière tuile placée sur le plateau.
     *
     * @return La dernière tuile placée sur le plateau, ou null si aucune tuile n'a été placée.
     */
    public PlacedTile lastPlacedTile() {
        if(indexes.length == 0){
            return null;
        }
        int lastTileIndex = indexes[indexes.length - 1];
        return placedTiles[lastTileIndex];
    }

    /**
     * Retourne l'ensemble des zones forêt fermées par la dernière tuile placée.
     *
     * @return L'ensemble des zones forêt fermées par la dernière tuile placée.
     */
    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {
        if (indexes.length == 0) { // If no tiles have been placed
            return Collections.emptySet();
        }
        int lastTileIndex = indexes[indexes.length - 1];
        PlacedTile lastTile = placedTiles[lastTileIndex];
        Set<Area<Zone.Forest>> closedForests = new HashSet<>();

        for (Zone.Forest forest : lastTile.forestZones()) {
            Area<Zone.Forest> forestArea = zonePartitions.forests().areaContaining(forest);
            if (forestArea != null && forestArea.isClosed()) { // Check if the forest area is closed
                closedForests.add(forestArea);
            }
        }
        return closedForests;
    }

    /**
     * Retourne l'ensemble des zones rivière fermées par la dernière tuile placée.
     *
     * @return L'ensemble des zones rivière fermées par la dernière tuile placée.
     */
    public Set<Area<Zone.River>> riversClosedByLastTile() {
        if (indexes.length == 0) { // If no tiles have been placed, return an empty set.
            return Collections.emptySet();
        }

        // Retrieve the last placed tile.
        int lastTileIndex = indexes[indexes.length - 1];
        PlacedTile lastTile = placedTiles[lastTileIndex];

        Set<Area<Zone.River>> closedRivers = new HashSet<>();

        // Iterate over each river zone of the last placed tile.
        for (Zone.River river : lastTile.riverZones()) {
            // Find the area containing the current river zone.
            Area<Zone.River> riverArea = zonePartitions.rivers().areaContaining(river);

            // If the river area is closed and not already included in the set, add it to the closedRivers set.
            if (riverArea != null && riverArea.isClosed() && !closedRivers.contains(riverArea)) {
                closedRivers.add(riverArea);
            }
        }

        return closedRivers;
    }

    /**
     * Vérifie si une tuile peut être ajoutée à une certaine position sur le plateau.
     *
     * @param tile La tuile à ajouter.
     * @return true si la tuile peut être ajoutée, sinon false.
     */
    public boolean canAddTile(PlacedTile tile) {
        if (!insertionPositions().contains(tile.pos())) {
            return false;
        }
        boolean valid = true;
        int i = 0;
        Direction[] directions = Direction.values();
        do {
            Pos adjacentPos = tile.pos().neighbor(directions[i]);
            PlacedTile adjacentTile = tileAt(adjacentPos);
            if (adjacentTile != null) {
                if (!tile.side(directions[i]).isSameKindAs(adjacentTile.side(directions[i].opposite()))) {
                    valid = false;
                    break;
                }
            }
            i++;
        } while (i < directions.length && valid);
        return valid;
    }

    /**
     * Vérifie si une tuile pourrait être placée sur le plateau.
     *
     * @param tile La tuile à placer.
     * @return true si la tuile pourrait être placée, sinon false.
     */
    public boolean couldPlaceTile(Tile tile){
        for (Pos position:insertionPositions()){
            for(int i=0;i<Rotation.ALL.size();i++){
                PlacedTile newPlacedTile=new PlacedTile(tile,null,Rotation.ALL.get(i),position);
                if(canAddTile(newPlacedTile)){return true;}
            }
        }
        return false;
    }

    /**
     * Ajoute une nouvelle tuile au plateau.
     *
     * @param tile La tuile à ajouter.
     * @return Le plateau avec la nouvelle tuile ajoutée.
     * @throws IllegalArgumentException Si la tuile ne peut pas être ajoutée ou si les limites du plateau ne sont pas respectées.
     */
    public Board withNewTile(PlacedTile tile) {
        Preconditions.checkArgument(indexes.length == 0 || canAddTile(tile));
        Preconditions.checkArgument(LimitOnBoardIsRespected(tile.pos()));
        PlacedTile[] updatedTiles = placedTiles.clone();
        int[] updatedIndexes = Arrays.copyOf(indexes, indexes.length + 1);
        ZonePartitions.Builder partitionBuilder = new ZonePartitions.Builder(zonePartitions);
        partitionBuilder.addTile(tile.tile());
        int tileIdx = (tile.pos().x() + REACH) + (tile.pos().y() + REACH) * LENGTH;

        updatedTiles[tileIdx] = tile;
        updatedIndexes[updatedIndexes.length - 1] = tileIdx;
        connectTileSides(tile, partitionBuilder);
        return new Board(updatedTiles, updatedIndexes, partitionBuilder.build(), cancelledAnimals);
    }
    private void connectTileSides(PlacedTile tile, ZonePartitions.Builder builder) {
        for (Direction direction : Direction.values()) {
            Pos adjacentPos = tile.pos().neighbor(direction);
            PlacedTile adjacentTile = tileAt(adjacentPos);
            if (adjacentTile != null && LimitOnBoardIsRespected(adjacentPos)) {
                builder.connectSides(tile.side(direction), adjacentTile.side(direction.opposite()));
            }
        }
    }

    private static int posToTileIndex(PlacedTile tile) {
        return (tile.pos().x() + REACH) + (tile.pos().y() + REACH) * LENGTH;

    }

    private static Zone findZoneBy(Occupant occupant, PlacedTile occupiedTile) {
        for(Zone zone : occupiedTile.tile().zones()) {
            if(zone.id() == occupant.zoneId()) {
                return zone;
            }
        }
        return null;
    }

    /**
     * Ajoute un occupant à une tuile spécifique sur le plateau.
     *
     * @param occupant L'occupant à ajouter.
     * @return Le plateau avec l'occupant ajouté.
     * @throws IllegalArgumentException Si l'occupant ne peut pas être ajouté ou si la zone de l'occupant est introuvable.
     */
    public Board withOccupant(Occupant occupant) {
        PlacedTile tile = tileWithId(Zone.tileId(occupant.zoneId()));
        Preconditions.checkArgument(tile != null);
        Preconditions.checkArgument(tile.occupant() == null);
        Zone matchingZone = null;
        for (Zone zone : tile.tile().zones()) {
            if (zone.id() == occupant.zoneId()) {
                matchingZone = zone;
                break;
            }
        }
        Preconditions.checkArgument(matchingZone != null);
        PlacedTile[] updatedTiles = new PlacedTile[placedTiles.length];
        for (int i = 0; i < placedTiles.length; i++) {
            updatedTiles[i] = placedTiles[i];
        }
        int tileIdx = (tile.pos().x() + REACH) + (tile.pos().y() + REACH) * LENGTH;
        updatedTiles[tileIdx] = tile.withOccupant(occupant);
        ZonePartitions.Builder partitionBuilder = new ZonePartitions.Builder(zonePartitions);
        partitionBuilder.addInitialOccupant(tile.placer(), occupant.kind(), matchingZone);
        return new Board(updatedTiles, indexes, partitionBuilder.build(), cancelledAnimals);
    }

    /**
     * Supprime un occupant d'une tuile spécifique sur le plateau.
     *
     * @param occupant L'occupant à supprimer.
     * @return Le plateau avec l'occupant supprimé.
     * @throws IllegalArgumentException Si la zone de l'occupant est introuvable.
     */
    public Board withoutOccupant(Occupant occupant) {
        PlacedTile occupiedTile = tileWithId(Zone.tileId(occupant.zoneId()));
        Preconditions.checkArgument(occupiedTile != null);
        Zone matchingZone = null;
        for (Zone zone : occupiedTile.tile().zones()) {
            if (zone.id() == occupant.zoneId()) {
                matchingZone = zone;
                break;
            }
        }
        Preconditions.checkArgument(matchingZone != null);
        PlacedTile[] updatedTiles = new PlacedTile[placedTiles.length];
        for (int i = 0; i < placedTiles.length; i++) {
            updatedTiles[i] = placedTiles[i];
        }
        int tileIdx = (occupiedTile.pos().x() + REACH) + (occupiedTile.pos().y() + REACH) * LENGTH;
        updatedTiles[tileIdx] = occupiedTile.withNoOccupant();
        ZonePartitions.Builder partitionBuilder = new ZonePartitions.Builder(zonePartitions);
        partitionBuilder.removePawn(occupiedTile.placer(), matchingZone);
        return new Board(updatedTiles, indexes, partitionBuilder.build(), cancelledAnimals);
    }

    /**
     * Supprime les cueilleurs et les pêcheurs des aires forêt et rivière spécifiées sur le plateau.
     *
     * @param forests L'ensemble des aires forêt à vérifier.
     * @param rivers L'ensemble des aires rivière à vérifier.
     * @return Le plateau avec les cueilleurs et les pêcheurs supprimés des aires spécifiées.
     */
    public Board withoutGatherersOrFishersIn(Set<Area<Zone.Forest>> forests, Set<Area<Zone.River>> rivers) {
        PlacedTile[] newPlacedTiles = placedTiles.clone();
        ZonePartitions.Builder newZonePartitions = new ZonePartitions.Builder(zonePartitions);

        for(Area<Zone.Forest> forestArea : forests){
            newZonePartitions.clearGatherers(forestArea);
        }

        for(Area<Zone.River> riverArea : rivers){
            newZonePartitions.clearFishers(riverArea);
        }

        Set<Integer> forestZoneIds = new HashSet<>();
        Set<Integer> riverZoneIds = new HashSet<>();

        for(Area<Zone.Forest> forestArea : forests){
            for(Zone.Forest forestZone : forestArea.zones()){
                forestZoneIds.add(forestZone.id());
            }
        }

        for(Area<Zone.River> riverArea : rivers){
            for(Zone.River riverZone : riverArea.zones()){
                riverZoneIds.add(riverZone.id());
            }
        }

        for (int i = 0; i < placedTiles.length; i++) {
            PlacedTile placedTile = placedTiles[i];
            if (placedTile != null && placedTile.occupant() != null) {
                int zoneId = placedTile.occupant().zoneId();
                if ((riverZoneIds.contains(zoneId) || forestZoneIds.contains(zoneId)) && placedTile.occupant().kind() == Occupant.Kind.PAWN) {
                    PlacedTile newTile = new PlacedTile(placedTile.tile(), placedTile.placer(), placedTile.rotation(), placedTile.pos());
                    newPlacedTiles[i] = newTile;

                }
            }
        }
        return new Board(newPlacedTiles, indexes, newZonePartitions.build(), cancelledAnimals);
    }

    /**
     * Ajoute des animaux annulés supplémentaires au plateau.
     *
     * @param newlyCancelledAnimals L'ensemble des animaux annulés supplémentaires à ajouter.
     * @return Le plateau avec les nouveaux animaux annulés ajoutés.
     */
    public Board withMoreCancelledAnimals(Set<Animal> newlyCancelledAnimals) {
        Set<Animal> updatedCancelledAnimals = new HashSet<>(this.cancelledAnimals);
        updatedCancelledAnimals.addAll(newlyCancelledAnimals);

        return new Board(this.placedTiles, this.indexes, this.zonePartitions, updatedCancelledAnimals);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Board other)) return false;

        return Arrays.equals(this.placedTiles, other.placedTiles) &&
                Arrays.equals(this.indexes, other.indexes) &&
                Objects.equals(this.zonePartitions, other.zonePartitions) &&
                Objects.equals(this.cancelledAnimals, other.cancelledAnimals);
    }

    @Override
    public int hashCode() {
        int hashPlacedTiles = Arrays.hashCode(placedTiles);
        int hashIndex = Arrays.hashCode(indexes);

        return Objects.hash(hashPlacedTiles, hashIndex, zonePartitions, cancelledAnimals);
    }
}