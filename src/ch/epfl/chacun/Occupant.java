    package ch.epfl.chacun;

    import java.util.Objects;

    /**
     * Occupant d'une aire de jeu.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record Occupant(Kind kind, int zoneId) {

        public enum Kind{
            PAWN,// représente un pion
            HUT;// représente une hutte
        }


        /**
         * Construit un occupant avec le type spécifié et l'identifiant de zone associé.
         *
         * @param kind   Le type d'occupant.
         * @param zoneId L'identifiant de la zone où réside l'occupant (doit être positif ou nul).
         * @throws NullPointerException     si le type d'occupant (kind) est null.
         * @throws IllegalArgumentException si l'identifiant de la zone (zoneId) est négatif.
         */
        public Occupant{
            Objects.requireNonNull(kind, "Le paramètre 'kind' ne peut pas être null.");
            Preconditions.checkArgument(zoneId>=0);
        }


        /**
         * Retourne le nombre d'occupants pour le type spécifié.
         *
         * @param kind Le type d'occupant.
         * @return Le nombre d'occupants correspondant au type spécifié.
         * @throws AssertionError si le type d'occupant n'est pas valide.
         */
        public static int occupantsCount(Kind kind){
            switch(kind){
                case HUT:
                    return 3;
                case PAWN:
                    return 5;
                default:
                    throw new AssertionError("Not valid kind");
            }
        }



    }
