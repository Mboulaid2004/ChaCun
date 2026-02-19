    package ch.epfl.chacun;

    import java.nio.file.attribute.PosixFileAttributes;
    import java.util.List;
    import java.util.Objects;

    public enum Direction {
        N,// correspond au nord
        E,// correspond à l'est
        S,// correspond au sud
        W;// correspond à l'ouest
        public static final List<Direction> ALL = List.of(Direction.values());
        public static final int COUNT = ALL.size();

        /**
         * Retourne la direction correspondant à l'application de la rotation donnée au récepteur.
         *
         * @param rotation La rotation à appliquer à la direction actuelle.
         * @return La direction résultante après l'application de la rotation.
         * @throws NullPointerException si la rotation donnée est nulle.
         */
        public Direction rotated(Rotation rotation){
            Objects.requireNonNull(rotation);
            int numberOfquarters = rotation.quarterTurnsCW();
            int sum=(this.ordinal() + numberOfquarters)%COUNT;

            return Direction.values()[sum];
        }

        /**
         * Retourne la direction opposée à celle du récepteur.
         *
         * @return La direction opposée à celle du récepteur.
         * @throws AssertionError si la direction actuelle n'est pas valide.
         */
        public Direction opposite(){
            return rotated(Rotation.HALF_TURN);
        }

    }
