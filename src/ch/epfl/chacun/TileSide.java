    package ch.epfl.chacun;

    import java.util.List;

    public sealed interface TileSide {
        public abstract List<Zone> zones();
        public  abstract boolean isSameKindAs(TileSide that);

        public record Forest(Zone.Forest forest)implements TileSide{

            /**
             * Retourne les zones forêt associées à ce côté de tuile.
             *
             * @return La liste des zones associées à ce côté de tuile.
             */
            public List<Zone>zones(){
                return List.of(forest) ;
            }

            /**
             * Vérifie si ce côté de tuile est du même type que celui spécifié.
             *
             * @param that Le côté de tuile à comparer.
             * @return {@code true} si les deux côtés sont du même type, sinon {@code false}.
             */
            public boolean isSameKindAs(TileSide that){
                return that instanceof TileSide.Forest;
            }
        }
        public record Meadow(Zone.Meadow meadow)implements TileSide{

            /**
             * Retourne les zones pré associées à ce côté de tuile.
             *
             * @return La liste des zones associées à ce côté de tuile.
             */
            public List<Zone>zones(){
                return List.of(meadow) ;
            }

            /**
             * Vérifie si ce côté de tuile est du même type que celui spécifié.
             *
             * @param that Le côté de tuile à comparer.
             * @return {@code true} si les deux côtés sont du même type, sinon {@code false}.
             */
            public boolean isSameKindAs(TileSide that){
                return that instanceof TileSide.Meadow;
            }
        }


        public record River(Zone.Meadow meadow1,Zone.River river,Zone.Meadow meadow2)implements TileSide{

            /**
             * Retourne les zones rivière associées à ce côté de tuile.
             *
             * @return La liste des zones associées à ce côté de tuile.
             */
            public List<Zone>zones(){
                return List.of(meadow1,river,meadow2) ;
            }

            /**
             * Vérifie si ce côté de tuile est du même type que celui spécifié.
             *
             * @param that Le côté de tuile à comparer.
             * @return {@code true} si les deux côtés sont du même type, sinon {@code false}.
             */
            public boolean isSameKindAs(TileSide that){
                return that instanceof TileSide.River;
            }
        }



    }
