    package ch.epfl.chacun;

    public record Animal(int id, Kind kind) {
        public enum Kind{
            MAMMOTH,
            AUROCHS,
            DEER,
            TIGER
        }

        /**.
         *
         * @return L'identifiant de la tuile où se trouve un animal.
         */
        public int tileId(){
            return Zone.tileId(id/10);
        }
    }
