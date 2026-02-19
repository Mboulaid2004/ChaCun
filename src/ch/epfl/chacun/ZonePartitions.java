    package ch.epfl.chacun;

    import java.time.zone.ZoneRules;
    import java.util.ArrayList;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    /**
     * Regroupement de différente partitions de zones.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record ZonePartitions(ZonePartition<Zone.Forest> forests, ZonePartition<Zone.Meadow> meadows, ZonePartition<Zone.River> rivers, ZonePartition<Zone.Water> riverSystems) {

        /**
         * Représente une instance vide de {@code ZonePartitions}.
         */
        public final static ZonePartitions EMPTY = new ZonePartitions(new ZonePartition<>(),new ZonePartition<>(),
                new ZonePartition<>(),new ZonePartition<>());

        public static final class Builder{
            private final ZonePartition.Builder<Zone.Forest> forestBuilder;
            private final ZonePartition.Builder<Zone.Meadow> meadowBuilder;
            private final ZonePartition.Builder<Zone.River> riverBuilder;
            private final ZonePartition.Builder<Zone.Water> riverSystemsBuilder;


            /**
             * Crée un nouveau constructeur de {@code ZonePartitions} à partir d'une instance existante.
             *
             * @param initial L'instance existante à partir de laquelle créer le constructeur.
             */
            public Builder(ZonePartitions initial){
                forestBuilder = new ZonePartition.Builder<>(initial.forests);
                meadowBuilder = new ZonePartition.Builder<>(initial.meadows);
                riverBuilder = new ZonePartition.Builder<>(initial.rivers);
                riverSystemsBuilder = new ZonePartition.Builder<>(initial.riverSystems);
            }

            /**
             * Ajoute une tuile avec ses zones à chaque partition en fonction des connexions ouvertes.
             *
             * @param tile La tuile à ajouter.
             */
            public void addTile(Tile tile) {
                int[] openConnections = new int[10];

                for (TileSide side : tile.sides()) {
                    for (Zone zone : side.zones()) {
                        openConnections[zone.localId()]++;
                        if (zone instanceof Zone.River river && river.hasLake()) {
                            openConnections[river.lake().localId()]++;
                            openConnections[river.localId()]++;
                        }
                    }
                }

                for (Zone zone : tile.zones()) {
                    switch (zone) {
                        case Zone.Forest forest -> forestBuilder.addSingleton(forest, openConnections[zone.localId()]);
                        case Zone.Meadow meadow -> meadowBuilder.addSingleton(meadow, openConnections[zone.localId()]);
                        case Zone.River river -> {
                            if (river.hasLake()) {
                                riverBuilder.addSingleton(river, openConnections[zone.localId()] -1);
                            } else {
                                riverBuilder.addSingleton(river, openConnections[zone.localId()]);
                            }
                            riverSystemsBuilder.addSingleton(river, openConnections[zone.localId()]);
                        }
                        case Zone.Lake lake -> riverSystemsBuilder.addSingleton(lake, openConnections[zone.localId()]);
                    }
                }
                for(Zone zone : tile.zones()){
                    if(zone instanceof Zone.River river){
                        if(river.hasLake()){
                            riverSystemsBuilder.union(river,river.lake());
                        }
                    }
                }
            }


            /**
             * Connecte deux {@code TileSide} correspondants entre les partitions.
             *
             * @param s1 Le premier côté de tuile à connecter.
             * @param s2 Le deuxième côté de tuile à connecter.
             * @throws IllegalArgumentException Si les {@code TileSide} ne sont pas de la même sorte.
             */
            public void connectSides(TileSide s1,TileSide s2){
                switch(s1){
                    case TileSide.Forest(Zone.Forest f1)
                            when s2 instanceof TileSide.Forest(Zone.Forest f2)->
                            forestBuilder.union(f1,f2);
                    case TileSide.Meadow(Zone.Meadow m1)
                            when s2 instanceof TileSide.Meadow(Zone.Meadow m2)->
                            meadowBuilder.union(m1,m2);
                    case TileSide.River(Zone.Meadow m,Zone.River r1,Zone.Meadow m1)
                            when s2 instanceof TileSide.River(Zone.Meadow m2, Zone.River r2, Zone.Meadow m22) ->{
                        riverBuilder.union(r1,r2);
                        riverSystemsBuilder.union(r1,r2);
                        meadowBuilder.union(m,m22);
                        meadowBuilder.union(m1,m2);

                    }

                    default -> throw new IllegalArgumentException("Les bords ne sont pas de la meme sorte");
                }
            }

            /**
             * Ajoute un occupant initial à une zone spécifiée dans la partition correspondante.
             *
             * @param player Le joueur à ajouter.
             * @param occupantKind Le type d'occupant.
             * @param occupiedZone La zone occupée.
             * @throws IllegalArgumentException Si le type d'occupant ne peut pas occuper la zone donnée.
             */
            public void addInitialOccupant(PlayerColor player, Occupant.Kind occupantKind, Zone occupiedZone){
                switch(occupiedZone){
                    case Zone.Forest f
                            when occupantKind.equals(Occupant.Kind.PAWN) ->
                            forestBuilder.addInitialOccupant(f,player);

                    case Zone.Meadow m
                            when occupantKind.equals(Occupant.Kind.PAWN) ->
                            meadowBuilder.addInitialOccupant(m,player);

                    case Zone.River r
                            when occupantKind.equals(Occupant.Kind.PAWN) ->
                            riverBuilder.addInitialOccupant(r,player);

                    case Zone.Water rs
                            when occupantKind.equals(Occupant.Kind.HUT) ->
                            riverSystemsBuilder.addInitialOccupant(rs,player);

                    default ->
                            throw new IllegalArgumentException("La sorte d'occupant donnée ne peut pas occuper la zone donnée");
                }
            }

            /**
             * Retire un pion d'un joueur d'une zone spécifiée dans la partition correspondante.
             *
             * @param player Le joueur à retirer.
             * @param occupiedZone La zone occupée.
             * @throws IllegalArgumentException Si la zone donnée ne peut pas contenir de pion.
             */
            public void removePawn(PlayerColor player, Zone occupiedZone){
                switch(occupiedZone){
                    case  Zone.Forest(int id, Zone.Forest.Kind kind)->
                            forestBuilder.removeOccupant(new Zone.Forest(id,kind), player);

                    case Zone.Meadow(int id,List<Animal> animals, Zone.SpecialPower specialPower ) ->
                            meadowBuilder.removeOccupant(new Zone.Meadow(id,animals,specialPower),player);

                    case Zone.River(int id, int fishCount, Zone.Lake lake) ->
                            riverBuilder.removeOccupant(new Zone.River(id,fishCount,lake),player);

                    default -> throw new IllegalArgumentException("La zone donnée ne peut pas contenir de pion");
                }
            }

            /**
             * Retire tous les récolteurs d'une zone de forêt spécifiée de la partition correspondante.
             *
             * @param forest La zone de forêt à nettoyer.
             */
            public void clearGatherers(Area<Zone.Forest> forest){
                forestBuilder.removeAllOccupantsOf(forest);
            }

            /**
             * Retire tous les pêcheurs d'une zone de rivière spécifiée de la partition correspondante.
             *
             * @param river La zone de rivière à nettoyer.
             */
            public void clearFishers(Area<Zone.River> river){
                riverBuilder.removeAllOccupantsOf(river);
            }

            /**
             * Construit une nouvelle instance de {@code ZonePartitions} à partir des partitions construites.
             *
             * @return Une nouvelle instance de {@code ZonePartitions}.
             */
            public ZonePartitions build(){
                return new ZonePartitions(forestBuilder.build(),meadowBuilder.build(),riverBuilder.build(),riverSystemsBuilder.build());
            }

        }





    }
