    package ch.epfl.chacun;

    import java.util.*;

    /**
     * Aire de zone spécifique.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record Area<Z extends Zone>(Set<Z> zones, List<PlayerColor> occupants, int openConnections) {

        /**
         * Constructeur de la classe {@code Area}.
         * @param zones L'ensemble des zones d'un certain type d'une aire.
         * @param occupants Les différents occupants d'une aire.
         * @param openConnections Les connexions ouvertes de l'aire.
         */
        public Area{
            Preconditions.checkArgument(openConnections>=0);
            Objects.requireNonNull(zones, "Les zones ne peuvent pas être nulles.");
            Objects.requireNonNull(occupants, "Les occupants ne peuvent pas être nuls.");

            List<PlayerColor> sortedOccupants = new ArrayList<>(occupants);
            Collections.sort(sortedOccupants);
            occupants = List.copyOf(sortedOccupants);
            zones = Set.copyOf(zones);
        }

        /**
         * Vérifie si une zone forêt a un menhir.
         *
         * @param forest La zone de forêt à vérifier.
         * @return true si une zone de forêt a un menhir, sinon false.
         */
        public static boolean hasMenhir(Area<Zone.Forest> forest) {
            for (Zone.Forest zone : forest.zones()) {
                if (zone.kind() == Zone.Forest.Kind.WITH_MENHIR) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Compte le nombre de groupes de champignons dans une zone forêt.
         *
         * @param forest La zone de forêt à inspecter.
         * @return Le nombre de groupes de champignons dans la zone de forêt.
         */
        public static int mushroomGroupCount(Area<Zone.Forest> forest) {
            int mushroomGroups = 0;
            for (Zone.Forest zone : forest.zones()) {
                if (zone.kind() == Zone.Forest.Kind.WITH_MUSHROOMS) {
                    mushroomGroups++;
                }
            }
            return mushroomGroups;
        }

        /**
         * Retourne les animaux présents dans une zone prairie, en excluant ceux annulés.
         *
         * @param meadowArea La zone de prairie à inspecter.
         * @param cancelledAnimals Les animaux annulés.
         * @return Les animaux présents dans la zone de prairie.
         */
        public static Set<Animal> animals(Area<Zone.Meadow> meadowArea, Set<Animal> cancelledAnimals) {
            Set<Animal> presentAnimals = new HashSet<>();
            for (Zone.Meadow meadow : meadowArea.zones()) {
                List<Animal> meadowAnimals = meadow.animals();

                for (Animal animal : meadowAnimals) {
                    if (!cancelledAnimals.contains(animal)) {
                        presentAnimals.add(animal);
                    }
                }
            }

            return presentAnimals;
        }

        /**
         * Calcule le nombre total de poissons dans une zone rivière, en incluant les poissons des lacs adjacents.
         *
         * @param riverArea La zone de rivière à inspecter.
         * @return Le nombre total de poissons dans la zone de rivière.
         */
        public static int riverFishCount(Area<Zone.River> riverArea) {
            int totalFish = 0;
            Set<Zone.Lake> countedLakes = new HashSet<>();


            for (Zone.River river : riverArea.zones()) {
                totalFish += river.fishCount();
                if (river.hasLake() && countedLakes.add(river.lake())) {
                    totalFish += river.lake().fishCount();
                }
            }
            return totalFish;
        }

        /**
         * Calcule le nombre total de poissons dans un réseau hydrographique, en incluant tous les types de zones d'eau.
         *
         * @param riverSystem Le système fluvial à inspecter.
         * @return Le nombre total de poissons dans le réseau hydrographique.
         */
        public static int riverSystemFishCount(Area <Zone.Water> riverSystem){
            int count = 0;
            for(Zone.Water zone : riverSystem.zones()){
                count += zone.fishCount();
            } return count;
        }

        /**
         * Compte le nombre de lacs distincts dans un réseau hydrographique.
         *
         * @param riverSystem Le système fluvial à inspecter.
         * @return Le nombre de lacs distincts dans le réseau hydrographiques.
         */
        public static int lakeCount(Area<Zone.Water> riverSystem) {
            Set<Zone.Lake> uniqueLakes = new HashSet<>();
            for (Zone.Water waterZone : riverSystem.zones()) {
                if (waterZone instanceof Zone.River river && river.hasLake()) {
                    uniqueLakes.add(river.lake());
                }
                else if (waterZone instanceof Zone.Lake lake) {
                    uniqueLakes.add(lake);
                }
            }
            return uniqueLakes.size();
        }

        /**
         * Vérifie si la zone est fermée, c'est-à-dire si elle n'a pas de connexions ouvertes.
         *
         * @return true si la zone est fermée, sinon false.
         */
        public boolean isClosed() {
            return this.openConnections == 0;
        }

        /**
         * Vérifie si la zone est occupée par des occupants.
         *
         * @return true si la zone est occupée, sinon false.
         */
        public boolean isOccupied() {
            return !occupants.isEmpty();
        }

        /**
         * Retourne l'ensemble des occupants qui sont majoritaires dans la zone.
         *
         * @return L'ensemble des occupants majoritaires.
         */
        public Set<PlayerColor> majorityOccupants() {
            List<PlayerColor> occupants = this.occupants(); // Assume this.occupants() returns the list of PlayerColor objects
            Set<PlayerColor> majority = new HashSet<>();
            int maxCount = 0;

            for (PlayerColor current : occupants) {
                int count = 0;
                for (PlayerColor other : occupants) {
                    if (current.equals(other)) {
                        count++;
                    }
                }
                if (count > maxCount) {
                    maxCount = count;
                    majority.clear();
                    majority.add(current);
                } else if (count == maxCount) {
                    majority.add(current);
                }
            }
            return majority;
        }

        /**
         * Connecte cette zone à une autre zone, en fusionnant leurs zones et occupants respectifs.
         *
         * @param that La zone à connecter à cette zone.
         * @return Une nouvelle zone résultant de la connexion entre cette zone et la zone spécifiée.
         * @throws IllegalArgumentException Si cette zone est égale à la zone spécifiée.
         */
        public Area<Z> connectTo(Area<Z> that) {

            if(this.equals(that)){
                int newOpenConnections = (this.openConnections) - 2;
                return new Area<>(this.zones,this.occupants, newOpenConnections);
            }

            Set<Z> newZones = new HashSet<>(this.zones);
            newZones.addAll(that.zones());
            List<PlayerColor> newOccupants = new ArrayList<>(this.occupants);
            newOccupants.addAll(that.occupants());

            int newOpenConnections = (this.openConnections + that.openConnections()) - 2;
            return new Area<>(newZones, newOccupants, newOpenConnections);
        }

        /**
         * Crée une nouvelle zone avec un seul occupant initial.
         *
         * @param occupant L'occupant initial de la nouvelle zone.
         * @return Une nouvelle zone avec l'occupant initial spécifié.
         * @throws IllegalArgumentException Si la zone contient déjà un occupant.
         */
        public Area<Z> withInitialOccupant(PlayerColor occupant) {
            if (!this.occupants.isEmpty()) {
                throw new IllegalArgumentException("L'aire est déjà occupée.");
            }
            List<PlayerColor> newOccupants = new ArrayList<>(List.of(occupant));
            return new Area<>(this.zones, newOccupants, this.openConnections);
        }

        /**
         * Retire un occupant spécifié de la zone.
         *
         * @param occupant L'occupant à retirer de la zone.
         * @return Une nouvelle zone sans l'occupant spécifié.
         * @throws IllegalArgumentException Si la zone ne contient pas l'occupant spécifié.
         */
        public Area<Z> withoutOccupant(PlayerColor occupant) {
            Preconditions.checkArgument(this.occupants.contains(occupant));

            List<PlayerColor> newOccupants = new ArrayList<>(this.occupants);
            newOccupants.remove(occupant);
            return new Area<>(this.zones, newOccupants, this.openConnections);
        }


        /**
         * Retire tous les occupants de la zone.
         *
         * @return Une nouvelle zone sans occupants.
         */
        public Area<Z> withoutOccupants() {
            return new Area<>(this.zones(), List.of(), this.openConnections());
        }

        /**
         * Retourne l'ensemble des identifiants de tuiles des zones contenues dans cette zone.
         *
         * @return L'ensemble des identifiants de tuiles.
         */
        public Set<Integer> tileIds() {
            Set<Integer> tileIds = new HashSet<>();
            for (Z zone : zones) {
                tileIds.add(zone.tileId());
            }
            return tileIds;
        }

        /**
         * Retourne la zone avec un pouvoir spécial spécifié, s'il existe dans cette zone.
         *
         * @param specialPower Le pouvoir spécial recherché.
         * @return La zone avec le pouvoir spécial spécifié, ou null si aucun n'est trouvé.
         */
        public Zone zoneWithSpecialPower(Zone.SpecialPower specialPower) {
            for (Zone zone : this.zones) {
                if (specialPower.equals(zone.specialPower())) {
                    return zone;
                }
            }
            return null;
        }






    }