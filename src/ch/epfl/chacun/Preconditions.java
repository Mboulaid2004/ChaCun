    package ch.epfl.chacun;

    /**
     * Fournit des méthodes utilitaires pour vérifier les préconditions.
     */
    public final class Preconditions {
        private Preconditions(){}

        /**
         * Vérifie si la condition spécifiée est vraie.
         *
         * @param shouldBeTrue La condition à vérifier.
         * @throws IllegalArgumentException si la condition n'est pas vraie.
         */
        public static void checkArgument(boolean shouldBeTrue){
            if (!shouldBeTrue)
                throw new IllegalArgumentException();
        }

    }
