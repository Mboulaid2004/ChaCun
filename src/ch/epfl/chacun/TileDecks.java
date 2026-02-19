    package ch.epfl.chacun;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.function.Predicate;

    /**
     * Paquets de tuiles du plateau de jeu.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record TileDecks(List<Tile> startTiles,List<Tile> normalTiles, List<Tile> menhirTiles) {

        /**
         * Constructeur de la classe `TileDecks`.
         *
         * @param startTiles    Les tuiles de départ.
         * @param normalTiles   Les tuiles normales.
         * @param menhirTiles   Les tuiles menhir.
         */
        public TileDecks {
            startTiles = List.copyOf(startTiles);
            normalTiles = List.copyOf(normalTiles);
            menhirTiles = List.copyOf(menhirTiles);
        }

        /**
         * Retourne la taille du deck pour un type de tuile spécifié.
         *
         * @param kind  Le type de tuile.
         * @return      La taille du deck pour le type de tuile spécifié.
         */
        public int deckSize(Tile.Kind kind) {
            return switch (kind) {
                case START -> startTiles.size();
                case NORMAL -> normalTiles.size();
                case MENHIR -> menhirTiles.size();
            };
        }

        /**
         * Retourne la tuile située au sommet du deck pour un type de tuile spécifié.
         *
         * @param kind  Le type de tuile.
         * @return      La tuile située au sommet du deck pour le type de tuile spécifié, ou null si le deck est vide.
         */
        public Tile topTile(Tile.Kind kind) {
            Preconditions.checkArgument(kind != null);


            return switch (kind) {
                case START -> startTiles.isEmpty() ? null : startTiles.get(0);
                case NORMAL -> normalTiles.isEmpty() ? null : normalTiles.get(0);
                case MENHIR -> menhirTiles.isEmpty() ? null : menhirTiles.get(0);
            };
        }

        /**
         * Retourne un nouveau deck avec la tuile du dessus retirée pour un type de tuile spécifié.
         *
         * @param kind  Le type de tuile.
         * @return Un nouveau deck avec la tuile du dessus retirée pour le type de tuile spécifié.
         * @throws IllegalArgumentException si le deck est vide pour le type de tuile spécifié.
         */
        public TileDecks withTopTileDrawn(Tile.Kind kind) {
            Preconditions.checkArgument(deckSize(kind) > 0);
            return switch (kind) {
                case START -> new TileDecks(startTiles.subList(1, startTiles.size()), normalTiles, menhirTiles);
                case NORMAL -> new TileDecks(startTiles, normalTiles.subList(1, normalTiles.size()), menhirTiles);
                case MENHIR -> new TileDecks(startTiles, normalTiles, menhirTiles.subList(1, menhirTiles.size()));
            };
        }

        /**
         * Retourne un nouveau deck avec les tuiles du dessus retirées jusqu'à ce qu'un prédicat spécifié soit satisfait pour le type de tuile spécifié.
         *
         * @param kind      Le type de tuile.
         * @param predicate Le prédicat pour vérifier la tuile retirée.
         * @return          Un nouveau deck avec les tuiles du dessus retirées jusqu'à ce que le prédicat soit satisfait.
         * @throws IllegalArgumentException si le deck est vide pour le type de tuile spécifié.
         */
        public TileDecks withTopTileDrawnUntil(Tile.Kind kind, Predicate<Tile> predicate) {
            TileDecks current = this;

            Preconditions.checkArgument(deckSize(kind) > 0);
            while (current.deckSize(kind) > 0 && !predicate.test(current.topTile(kind))) {
                current = current.withTopTileDrawn(kind);
            }

            return current;
        }

    }
