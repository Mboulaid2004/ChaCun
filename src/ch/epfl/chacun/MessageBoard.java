    package ch.epfl.chacun;

    import java.util.*;
    import java.util.stream.Collectors;

    /**
     * Tableau d'affichage des évenements importants.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record MessageBoard(TextMaker textMaker, List<Message> messages) {

        /**
         * Représente un message sur le tableau d'affichage.
         * Chaque message contient du texte, des points, des joueurs ayant marqué des points et des IDs de tuiles associés.
         */
        public record Message (String text, int points, Set<PlayerColor> scorers, Set<Integer> tileIds) {

            /**
             * Construit un message avec les paramètres spécifiés.
             *
             * @param text Le texte du message.
             * @param points Les points associés au message.
             * @param scorers L'ensemble des couleurs de joueur ayant marqué des points.
             * @param tileIds L'ensemble des IDs de tuiles associés au message.
             * @throws NullPointerException si l'un des paramètres est null.
             * @throws IllegalArgumentException si points est négatif.
             */
            public Message {
                Objects.requireNonNull(text, "Text cannot be null");
                Objects.requireNonNull(scorers, "Scorers cannot be null");
                Objects.requireNonNull(tileIds, "Tile IDs cannot be null");
                Preconditions.checkArgument(points >= 0);
                scorers = Set.copyOf(scorers);
                tileIds = Set.copyOf(tileIds);
            }
        }
        public MessageBoard {
            Preconditions.checkArgument(textMaker != null);
            Preconditions.checkArgument(messages != null);
            messages = List.copyOf(messages);
        }

        /**
         * Retourne une table associant à tous les joueurs figurant dans les gagnants d'au moins un message,
         * le nombre total de points obtenus.
         *
         * @return Une table associative associant les joueurs aux points obtenus.
         */
        public Map<PlayerColor, Integer> points() {
            Map<PlayerColor, Integer> pointsMap = new HashMap<>();
            for (Message message : this.messages) {
                Set<PlayerColor> scorers = message.scorers();
                for (PlayerColor scorer : scorers) {
                    Integer currentPoints = pointsMap.get(scorer);
                    if (currentPoints == null) {
                        currentPoints = 0;
                    }
                    pointsMap.put(scorer, currentPoints + message.points());
                }
            }
            return pointsMap;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la forêt donnée est occupée,
         * auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires
         * ont remporté les points associés à sa fermeture.
         *
         * @param forestArea La zone de forêt à vérifier.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la forêt est fermée et occupée.
         */
        public MessageBoard withScoredForest(Area<Zone.Forest> forestArea) {
            if (!forestArea.isClosed() || !forestArea.isOccupied()) {
                return this;
            }
            int tileCount = forestArea.zones().size();
            int mushroomGroupCount = Area.mushroomGroupCount(forestArea);
            int points = Points.forClosedForest(tileCount, mushroomGroupCount);
            Set<PlayerColor> scorers = forestArea.majorityOccupants();
            String messageText = textMaker.playersScoredForest(scorers, points, mushroomGroupCount, tileCount);
            Message newMessage = new Message(messageText, points, scorers, forestArea.tileIds());
            List<Message> updatedMessages = new ArrayList<>(this.messages);
            updatedMessages.add(newMessage);
            return new MessageBoard(this.textMaker, updatedMessages);
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la forêt donnée est occupée,
         * auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires
         * ont remporté les points associés à sa fermeture avec une tuile Menhir.
         *
         * @param player Le joueur ayant fermé la forêt.
         * @param forest La zone de forêt à vérifier.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la forêt est fermée et occupée.
         */
        public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {
            boolean containsMenhir = forest.zones().stream()
                    .anyMatch(zone -> zone.kind() == Zone.Forest.Kind.WITH_MENHIR);
            if (containsMenhir) {
                String messageText = textMaker.playerClosedForestWithMenhir(player);
                Set<Integer> tileIds = forest.zones().stream()
                        .map(Zone::tileId)
                        .collect(Collectors.toSet());
                Message newMessage = new Message(messageText, 0, Set.of(), tileIds);
                List<Message> updatedMessages = new ArrayList<>(this.messages);
                updatedMessages.add(newMessage);
                return new MessageBoard(this.textMaker, updatedMessages);
            }
            return this;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la rivière donnée est occupée
         * et rapporte des points, auquel cas le tableau contient un nouveau message signalant que ses occupants
         * majoritaires ont remporté les points associés à sa fermeture.
         *
         * @param river La zone de rivière à vérifier.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la rivière est fermée et occupée.
         */
        public MessageBoard withScoredRiver(Area<Zone.River> river) {
            int points = Points.forClosedRiver(river.tileIds().size(), Area.riverFishCount(river));
            Set<PlayerColor> majorityOccupants = river.majorityOccupants();
            if (majorityOccupants.isEmpty() || points == 0) {
                return this;
            }
            String messageText = textMaker.playersScoredRiver(majorityOccupants, points, Area.riverFishCount(river), river.tileIds().size());
            Message newMessage = new Message(messageText, points, majorityOccupants, river.tileIds());
            List<Message> updatedMessages = new ArrayList<>(this.messages);
            updatedMessages.add(newMessage);
            return new MessageBoard(this.textMaker, updatedMessages);
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la pose de la fosse à pieux a permis
         * au joueur donné, qui l'a posée, de remporter des points, auquel cas le tableau contient un nouveau
         * message signalant cela.
         *
         * @param scorer Le joueur ayant posé la fosse à pieux.
         * @param adjacentMeadow La zone de pré adjacente à la fosse à pieux.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la pose de la fosse à pieux a rapporté des points.
         */
        public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals) {
            TreeMap<Animal.Kind, Integer> animalCounts = new TreeMap<>();
            //int deerToCancel = (int) cancelledAnimals.stream().filter(animal -> animal.kind() == Animal.Kind.TIGER).count();
            int points;
            Set<Animal> presentAnimals = Area.animals(adjacentMeadow, cancelledAnimals);

            for (Animal animal : presentAnimals) {
                animalCounts.put(animal.kind(), animalCounts.getOrDefault(animal.kind(), 0) + 1);
            }

            points = Points.forMeadow(
                    animalCounts.getOrDefault(Animal.Kind.MAMMOTH, 0),
                    animalCounts.getOrDefault(Animal.Kind.AUROCHS, 0),
                    animalCounts.getOrDefault(Animal.Kind.DEER, 0)
            );


            if (points > 0) {
                String messageText = textMaker.playerScoredHuntingTrap(scorer, points, animalCounts);
                Message newMessage = new Message(messageText, points, Set.of(scorer), adjacentMeadow.tileIds());
                List<Message> updatedMessages = new ArrayList<>(this.messages);
                updatedMessages.add(newMessage);
                return new MessageBoard(this.textMaker, updatedMessages);
            }
            return this;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la pose de la pirogue dans le réseau
         * hydrographique donné a permis au joueur donné de remporter des points, auquel cas le tableau contient
         * un nouveau message signalant cela.
         *
         * @param scorer Le joueur ayant posé la pirogue.
         * @param riverSystem Le réseau hydrographique où la pirogue a été posée.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la pose de la pirogue a rapporté des points.
         */
        public MessageBoard withScoredLogboat(PlayerColor scorer, Area<Zone.Water> riverSystem) {
            int lakeCount = Area.lakeCount(riverSystem);
            int points = Points.forLogboat(lakeCount);
            String messageText = textMaker.playerScoredLogboat(scorer, points, lakeCount);
            Message newMessage = new Message(messageText, points, Set.of(scorer), riverSystem.tileIds());
            List<Message> updatedMessages = new ArrayList<>(this.messages);
            updatedMessages.add(newMessage);
            return new MessageBoard(this.textMaker, updatedMessages);
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si le pré donné est occupé
         * et rapporte des points, auquel cas le tableau contient un nouveau message signalant que
         * ses occupants majoritaires ont remporté les points associés à sa fermeture.
         *
         * @param meadow La zone de pré à vérifier.
         * @param cancelledAnimals Les animaux annulés dans la zone de pré.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si le pré est fermé et rapporte des points.
         */
        public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {
            if (!meadow.isOccupied()) {
                return this;
            }
            Set<Animal> presentAnimals = Area.animals(meadow, cancelledAnimals);
            TreeMap<Animal.Kind, Integer> animalCounts = new TreeMap<>();
            for (Animal animal : presentAnimals) {
                if (!cancelledAnimals.contains(animal)) {
                    animalCounts.merge(animal.kind(), 1, Integer::sum);
                }
            }
            int points = Points.forMeadow(
                    animalCounts.getOrDefault(Animal.Kind.MAMMOTH, 0),
                    animalCounts.getOrDefault(Animal.Kind.AUROCHS, 0),
                    animalCounts.getOrDefault(Animal.Kind.DEER, 0)
            );
            if (points > 0) {
                Set<PlayerColor> majorityOccupants = meadow.majorityOccupants();
                String messageText = textMaker.playersScoredMeadow(majorityOccupants, points, animalCounts);
                Message newMessage = new Message(messageText, points, majorityOccupants, meadow.tileIds());
                List<Message> updatedMessages = new ArrayList<>(this.messages);
                updatedMessages.add(newMessage);
                return new MessageBoard(this.textMaker, updatedMessages);
            }
            return this;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné est occupé
         * et rapporte des points, auquel cas le tableau contient un nouveau message signalant que ses occupants
         * majoritaires ont remporté les points associés à sa fermeture.
         *
         * @param riverSystem Le réseau hydrographique à vérifier.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si le réseau hydrographique est fermé et rapporte des points.
         */
        public MessageBoard withScoredRiverSystem(Area<Zone.Water> riverSystem) {
            int fishCount = Area.riverSystemFishCount(riverSystem);

            if (fishCount > 0 && riverSystem.isOccupied()) {
                Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
                int points = Points.forRiverSystem(fishCount);
                String messageText = textMaker.playersScoredRiverSystem(majorityOccupants, points, fishCount);
                Message newMessage = new Message(messageText, points, majorityOccupants, riverSystem.tileIds());
                List<Message> updatedMessages = new ArrayList<>(this.messages);
                updatedMessages.add(newMessage);

                return new MessageBoard(this.textMaker, updatedMessages);
            }
            return this;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si la fosse à pieux adjacente donnée
         * est occupée et rapporte des points, auquel cas le tableau contient un nouveau message signalant que
         * ses occupants majoritaires ont remporté les points associés à sa fermeture.
         *
         * @param adjacentMeadow Le pré adjacent à la fosse à pieux à vérifier.
         * @param cancelledAnimals Les animaux annulés par la fosse à pieux.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si la fosse à pieux adjacente est fermée et rapporte des points.
         */
        public MessageBoard withScoredPitTrap(Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals) {
            Set<Animal> presentAnimals = Area.animals(adjacentMeadow, cancelledAnimals);

            TreeMap<Animal.Kind, Integer> animalCounts = new TreeMap<>();
            for (Animal animal : presentAnimals) {
                animalCounts.merge(animal.kind(), 1, Integer::sum);
            }

            int points = Points.forMeadow(
                    animalCounts.getOrDefault(Animal.Kind.MAMMOTH, 0),
                    animalCounts.getOrDefault(Animal.Kind.AUROCHS, 0),
                    animalCounts.getOrDefault(Animal.Kind.DEER, 0)
            );

            if (points > 0 && !adjacentMeadow.occupants().isEmpty()) {
                Set<PlayerColor> majorityOccupants = adjacentMeadow.majorityOccupants();

                String messageText = textMaker.playersScoredPitTrap(majorityOccupants, points, animalCounts);

                Message newMessage = new Message(messageText, points, majorityOccupants, adjacentMeadow.tileIds());

                List<Message> updatedMessages = new ArrayList<>(this.messages);
                updatedMessages.add(newMessage);

                return new MessageBoard(this.textMaker, updatedMessages);
            }

            return this;
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné,
         * qui contient le radeau, est occupé, auquel cas le tableau contient un nouveau message signalant
         * que ses occupants majoritaires ont remporté les points correspondants.
         *
         * @param riverSystem Le réseau hydrographique contenant le radeau.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire si le réseau hydrographique contenant le radeau est fermé.
         */
        public MessageBoard withScoredRaft(Area<Zone.Water> riverSystem) {
            int lakeCount = Area.lakeCount(riverSystem);
            Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
            if (majorityOccupants.isEmpty()) {
                return this;
            }
            int points = Points.forRaft(lakeCount);
            String messageText = textMaker.playersScoredRaft(majorityOccupants, points, lakeCount);
            Message newMessage = new Message(messageText, points, majorityOccupants, riverSystem.tileIds());
            List<Message> updatedMessages = new ArrayList<>(this.messages);
            updatedMessages.add(newMessage);
            return new MessageBoard(this.textMaker, updatedMessages);
        }

        /**
         * Retourne un tableau d'affichage identique au récepteur, sauf si le(s) joueur(s) donné(s) a/ont remporté
         * la partie avec le nombre de points donnés, auquel cas le tableau contient un nouveau message signalant cela.
         *
         * @param winners Les joueurs gagnants.
         * @param points  Le nombre de points remportés.
         * @return Un nouveau tableau d'affichage avec un message supplémentaire indiquant les joueurs gagnants et leurs points.
         * @throws IllegalArgumentException Si l'ensemble des gagnants est null ou vide.
         *                                  Si le nombre de points est négatif.
         */
        public MessageBoard withWinners(Set<PlayerColor> winners, int points) {
            String messageText = textMaker.playersWon(winners, points);
            Message finalMessage = new Message(messageText, 0, winners, Set.of());
            List<Message> updatedMessages = new ArrayList<>(this.messages);
            updatedMessages.add(finalMessage);

            return new MessageBoard(this.textMaker, updatedMessages);
        }
    }
