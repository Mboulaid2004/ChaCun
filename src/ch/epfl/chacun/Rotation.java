    package ch.epfl.chacun;

    import java.util.List;

    /**
     * Énumère les rotations possibles d'une tuile.
     * {@code} NONE, RIGHT, HALF_TURN, LEFT
     */
    public enum Rotation {
        NONE,
        RIGHT,
        HALF_TURN,
        LEFT;

        /**
         * Liste de rotations possibles d'une tuile dans l'ordre de définition.
         */
        public static final List<Rotation> ALL = List.of(Rotation.values());

        /**
         * Le nombre d'éléments du type énuméré {@code Rotation}.
         */
        public static final int COUNT = ALL.size();

        /**
         * Retourne la somme de la rotation représentée par le récepteur (this) et l'argument (that).
         *
         * @param that La rotation à ajouter à la rotation actuelle.
         * @return La rotation résultante après l'addition.
         */
        public Rotation add(Rotation that){
            int somme = (this.ordinal() + that.ordinal()) % COUNT;
            return  Rotation.values()[somme];
        }

        /**
         * Retourne la négation de la rotation représentée par le récepteur.
         * Cela signifie la rotation qui, ajoutée au récepteur par la méthode {@code add}, produit la rotation nulle (NONE).
         *
         * @return La rotation résultante après la négation.
         * @throws AssertionError si la rotation actuelle n'est pas valide.
         */
        public Rotation negated(){
            return switch (this) {
                case NONE -> NONE;
                case RIGHT -> LEFT;
                case HALF_TURN -> HALF_TURN;
                case LEFT -> RIGHT;
                default -> throw new AssertionError(STR."Not valid rotation\{this}.");
            };
        }

        /**
         * Retourne le nombre de quarts de tours correspondant au récepteur dans le sens horaire (0, 1, 2 ou 3).
         * Le suffixe CW signifie clockwise (sens horaire).
         *
         * @return Le nombre de quarts de tours dans le sens horaire.
         */
        public int quarterTurnsCW(){
            return this.ordinal() % COUNT;
        }

        /**
         * Retourne l'angle correspondant au récepteur, en degrés, dans le sens horaire (0°, 90°, 180° ou 270°).
         *
         * @return L'angle en degrés dans le sens horaire.
         * @throws AssertionError si la rotation actuelle n'est pas valide.
         */
        public int degreesCW(){
            switch(this) {
                case NONE:
                    return 0;
                case RIGHT:
                    return 90;
                case HALF_TURN:
                    return 180;
                case LEFT:
                    return 270;

                default:
                    throw new AssertionError(STR."Not valid angle\{this}.");
            }
        }

    }
