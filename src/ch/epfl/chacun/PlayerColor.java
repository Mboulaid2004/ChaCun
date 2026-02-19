    package ch.epfl.chacun;

    import java.util.List;

    /**
     * Couleurs des joueurs.
     * {@code} RED, BLUE, GREEN, YELLOW, PURPLE
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public enum PlayerColor {
        RED,
        BLUE,
        GREEN,
        YELLOW,
        PURPLE;

        /**
         * Une liste immuable contenant la totalité des valeurs du type énuméré PlayerColor,
         * dans leur ordre de définition.
         */
        public static final List<PlayerColor> ALL = List.of(PlayerColor.values());





    }
