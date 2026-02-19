    package ch.epfl.chacun;


    import java.util.List;

    public sealed interface  Zone {

        /**
         * Énumère les différents pouvoirs spéciaux d'une zone.
         */
        enum SpecialPower{
            SHAMAN,
            LOGBOAT,
            HUNTING_TRAP,
            PIT_TRAP,
            WILD_FIRE,
            RAFT
        }

        /**
         * Retourne l'identifiant de la tuile à partir de l'identifiant de la zone.
         *
         * @param zoneId L'identifiant de la zone.
         * @return L'identifiant de la tuile correspondant à l'identifiant de la zone.
         */
        static int tileId(int zoneId){
            return zoneId/10;
        }


        /**
         * Retourne l'identifiant local à partir de l'identifiant de la zone.
         *
         * @param zoneId L'identifiant de la zone.
         * @return L'identifiant local correspondant à l'identifiant de la zone.
         */
        static int localId(int zoneId){
            return zoneId%10;
        }

        public abstract int id();

        /**
         * Retourne l'identifiant de la tuile où se trouve cette zone.
         *
         * @return L'identifiant de la tuile où se trouve cette zone.
         */
        default int tileId(){
             return tileId(id());
        }

        /**
         * Retourne l'identifiant local de cette zone.
         *
         * @return L'identifiant local de cette zone.
         */
        default int localId(){
            return localId(id());
        }

        /**
         * Retourne le pouvoir spécial de cette zone.
         *
         * @return Le pouvoir spécial de cette zone, ou {@code null} s'il n'y en a pas.
         */
        default SpecialPower specialPower(){
             return null;
        }


        record Forest(int id, Kind kind) implements Zone{
             public enum Kind{
                PLAIN,
                WITH_MENHIR,
                WITH_MUSHROOMS
            }
        }

        record Meadow(int id, List<Animal> animals, SpecialPower specialPower) implements Zone{
            public Meadow{
                animals = List.copyOf(animals);
            }
        }
        sealed interface Water extends Zone{
              int fishCount();
        }

        record Lake(int id, int fishCount, SpecialPower specialPower) implements Water{
        }

        record River(int id, int fishCount, Lake lake) implements Water{

            /**
             * Indique si cette rivière est associée à un lac.
             *
             * @return {@code true} si cette rivière est associée à un lac, sinon {@code false}.
             */
             public boolean hasLake(){
                 return this.lake != null;
             }
        }


    }
