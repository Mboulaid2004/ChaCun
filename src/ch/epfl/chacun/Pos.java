    package ch.epfl.chacun;

    /**
     * Position des tuiles du jeu.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record Pos(int x, int y) {

        public final static Pos ORIGIN = new Pos(0,0);

        /**
         * Déplace cette position de la quantité spécifiée selon les axes x et y.
         *
         * @param dx Le décalage sur l'axe des abscisses.
         * @param dy Le décalage sur l'axe des ordonnées.
         * @return Une nouvelle position représentant le résultat du déplacement.
         */
        public Pos translated(int dx, int dy) {
            return new Pos(this.x + dx, this.y + dy);
        }

        /**
         * Retourne la position voisine dans la direction spécifiée.
         *
         * @param direction La direction de la position voisine à récupérer.
         * @return La position voisine dans la direction spécifiée.
         */
        public Pos neighbor(Direction direction){
            return switch (direction) {
                case N -> new Pos(x, y - 1);
                case E -> new Pos(x + 1, y);
                case S -> new Pos(x, y + 1);
                case W -> new Pos(x - 1, y);
            };
        }

    }
