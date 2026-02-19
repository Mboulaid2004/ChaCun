    package ch.epfl.chacun;

    import java.util.*;

    /**
     * Partition de Zones.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record ZonePartition<Z extends Zone>(Set<Area<Z>> areas) {

        /**
         * Crée une nouvelle partition de zones avec les aires spécifiées.
         *
         * @param areas Les aires de la partition.
         */
        public ZonePartition(Set<Area<Z>> areas){
            this.areas = Set.copyOf(areas);
        }

        /**
         * Crée une nouvelle partition de zones vide.
         */
        public ZonePartition(){
            this(Set.of());
        }

        /**
         * Retourne l'aire contenant la zone spécifiée.
         *
         * @param zone La zone recherchée.
         * @return L'aire contenant la zone spécifiée.
         * @throws IllegalArgumentException Si la zone spécifiée n'appartient à aucune aire de la partition.
         */
        public Area<Z> areaContaining(Z zone){
            for(Area<Z> a : areas){
                if(a.zones().contains(zone)){ return a;}
            }
            throw new IllegalArgumentException("La zone en parametre n'appartient a aucune aire de la partition");
        }


        static private <Z extends Zone> Area<Z> areaContaining(Z zone, Set<Area<Z>> areas){
            for(Area<Z> a : areas){
                if(a.zones().contains(zone)){ return a;}
            }
            throw new IllegalArgumentException("La zone en parametre n'appartient a aucune aire de la partition");
        }

        /**
         * Représente un batisseur pour créer une partition de zones.
         *
         * @param <Z> Le type de zone contenu dans la partition.
         */
        public static final class Builder<Z extends Zone>{
            private HashSet<Area<Z>> areas;

            /**
             * Crée un nouveau constructeur de partition à partir d'une partition existante.
             *
             * @param zonePartition La partition existante à partir de laquelle créer le constructeur.
             */
            public Builder(ZonePartition<Z> zonePartition){
                areas = new HashSet<>(zonePartition.areas);
            }

            /**
             * Ajoute une zone avec un certain nombre de connexions ouvertes comme une aire individuelle à la partition.
             *
             * @param zone La zone à ajouter à la partition.
             * @param openConnections Le nombre de connexions ouvertes de la zone.
             */
            public void addSingleton(Z zone,int openConnections){
                areas.add(new Area<>(Set.of(zone), List.of(),openConnections));
            }

            /**
             * Ajoute un occupant initial à une zone spécifiée dans la partition.
             *
             * @param zone La zone à laquelle ajouter l'occupant initial.
             * @param color La couleur de l'occupant initial.
             * @throws IllegalArgumentException Si la zone est déjà occupée.
             */
            public void addInitialOccupant(Z zone, PlayerColor color){
                Area<Z> area = areaContaining(zone,areas);
                Preconditions.checkArgument(!area.isOccupied());

                areas.remove(area);

                Area<Z> newArea = area.withInitialOccupant(color);
                areas.add(newArea);
            }

            /**
             * Retire un occupant spécifié d'une zone dans la partition.
             *
             * @param zone La zone de laquelle retirer l'occupant.
             * @param color La couleur de l'occupant à retirer.
             * @throws IllegalArgumentException Si la zone ne contient pas l'occupant spécifié.
             */
            public void removeOccupant(Z zone, PlayerColor color){
                Area<Z> area = areaContaining(zone,this.areas);
                Preconditions.checkArgument(area.occupants().contains(color));
                areas.remove(area);
                areas.add(area.withoutOccupant(color));
            }

            /**
             * Retire tous les occupants d'une aire spécifiée dans la partition.
             *
             * @param area L'aire de laquelle retirer tous les occupants.
             * @throws IllegalArgumentException Si l'aire spécifiée n'est pas présente dans la partition.
             */
            public void removeAllOccupantsOf(Area<Z> area){
                Preconditions.checkArgument(areas.contains(area));
                areas.remove(area);
                areas.add(area.withoutOccupants());
            }

            /**
             * Fusionne deux zones dans la partition, les connectant en une seule aire.
             *
             * @param zone1 La première zone à fusionner.
             * @param zone2 La deuxième zone à fusionner.
             */
            public void union(Z zone1, Z zone2){
                Area<Z> area1 = areaContaining(zone1,areas);
                Area<Z> area2 = areaContaining(zone2,areas);
                Area<Z> newArea = area1.connectTo(area2);

                if(area1.equals(area2)){
                    areas.remove(area1);}
                else{
                    areas.remove(area1);
                    areas.remove(area2);
                }
                areas.add(newArea);
            }

            /**
             * Construit une nouvelle partition de zones à partir des aires spécifiées.
             *
             * @return Une nouvelle partition de zones.
             */
            public ZonePartition<Z> build(){
                return new ZonePartition<>(this.areas);
            }

        }

    }
