    package ch.epfl.chacun;

    import java.util.HashSet;
    import java.util.Objects;
    import java.util.Set;

    /**
     * Tuile qui a été placée.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos, Occupant occupant ) {
        public PlacedTile{
            Objects.requireNonNull(tile,"La tuile placée est nulle");
            Objects.requireNonNull(rotation,"La rotation est nulle");
            Objects.requireNonNull(pos,"La position est nulle");

        }

        public PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos){
            this(tile,placer,rotation,pos,null);
        }

        public int id(){
            return tile.id();
        }

        /**
         * Retourne la sorte de la tuile placée.
         *
         * @return la sorte de la tuile placée.
         */
        public Tile.Kind kind(){
            return tile.kind();
        }

        /**
         * Retourne le côté de la tuile dans la direction donnée, en tenant compte de la rotation appliquée à la tuile.
         *
         * @param direction la direction du côté souhaité
         * @return le côté de la tuile dans la direction donnée
         */
        public TileSide side(Direction direction){
            return tile.sides().get(direction.rotated(rotation.negated()).ordinal());

        }

        /**
         * Retourne la zone de la tuile dont l'identifiant est celui donné, ou lève IllegalArgumentException si la tuile ne possède pas de zone avec cet identifiant.
         *
         * @param id l'identifiant de la zone recherchée
         * @return la zone de la tuile avec l'identifiant spécifié
         * @throws IllegalArgumentException si la tuile ne possède pas de zone avec l'identifiant donné
         */
        public Zone zoneWithId(int id) {
            for (Zone z : tile.zones()) {
                if (z.id() == id) { return z;}
            }
            throw new IllegalArgumentException("La tuile ne possède pas de zone avec l'identifiant donné ");
        }

        /**
         * Retourne la zone de la tuile ayant un pouvoir spécial, ou null s'il n'y en a aucune.
         *
         * @return la zone de la tuile ayant un pouvoir spécial, ou null s'il n'y en a aucune
         */
        public Zone specialPowerZone(){
            for (Zone z : tile.zones()) {
                if(z.specialPower() != null){ return z;}
            }
            return null;
        }

        /**
         * Retourne l'ensemble des zones forêt de la tuile.
         *
         * @return l'ensemble des zones forêt de la tuile
         */
        public Set<Zone.Forest> forestZones(){
            Set<Zone.Forest> zoneforest = new HashSet<>();
            for(Zone z: tile.zones()){
                if(z instanceof Zone.Forest forest){ zoneforest.add(forest);}
            }
            return zoneforest;
        }

        /**
         * Retourne l'ensemble des zones prairie de la tuile.
         *
         * @return l'ensemble des zones prairie de la tuile
         */
        public Set<Zone.Meadow> meadowZones(){
            Set<Zone.Meadow> zonemeadow = new HashSet<>();
            for(Zone z: tile.zones()){
                if(z instanceof Zone.Meadow meadow){ zonemeadow.add(meadow);}
            }
            return zonemeadow;
        }

        /**
         * Retourne l'ensemble des zones rivière de la tuile.
         *
         * @return l'ensemble des zones rivière de la tuile
         */
        public Set<Zone.River> riverZones(){
            Set<Zone.River> zoneriver = new HashSet<>();
            for(Zone z: tile.zones()){
                if(z instanceof Zone.River river){ zoneriver.add(river);}
            }
            return zoneriver;
        }

        /**
         * Retourne l'ensemble des occupants potentiels sur les zones adjacentes à la tuile.
         *
         * @return l'ensemble des occupants potentiels sur les zones adjacentes à la tuile
         */
        public Set<Occupant> potentialOccupants(){
            Set<Occupant> potentialoccupants = new HashSet<>();

           if(placer == null){ return new HashSet<>();}
           else{
               for(Zone z: tile.sideZones()){
                   if(z instanceof Zone.River river){
                       if(river.hasLake()){
                           potentialoccupants.add(new Occupant(Occupant.Kind.HUT,river.lake().id()));
                       }
                       else{
                           potentialoccupants.add(new Occupant(Occupant.Kind.HUT,river.id()));
                       }
                   }
                   potentialoccupants.add(new Occupant(Occupant.Kind.PAWN,z.id()));
               }
               return potentialoccupants;
           }

        }

        /**
         * Retourne une nouvelle instance de PlacedTile avec l'occupant spécifié.
         *
         * @param occupant l'occupant à associer à la tuile
         * @return une nouvelle instance de `PlacedTile` avec l'occupant spécifié
         * @throws IllegalArgumentException si la tuile a déjà un occupant
         */
        public PlacedTile withOccupant(Occupant occupant){
            Preconditions.checkArgument(this.occupant == null);
            return new PlacedTile(this.tile,this.placer,this.rotation,this.pos,occupant);
        }

        /**
         * Retourne une nouvelle instance de `PlacedTile` sans occupant.
         *
         * @return une nouvelle instance de `PlacedTile` sans occupant
         */
        public PlacedTile withNoOccupant(){
            return new PlacedTile(this.tile,this.placer,this.rotation,this.pos,null);
        }

        /**
         * Retourne l'identifiant de la zone occupée par l'occupant du type spécifié.
         *
         * @param occupantKind le type de l'occupant dont l'identifiant de la zone est recherché
         * @return l'identifiant de la zone occupée par l'occupant du type spécifié, ou -1 s'il n'y a pas d'occupant de ce type
         */
        public int idOfZoneOccupiedBy(Occupant.Kind occupantKind){
            return (occupant != null && occupant.kind() == occupantKind) ? occupant.zoneId() : -1;
        }


    }




