    package ch.epfl.chacun;

    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    /**
     * Tuile du plateau de jeu.
     *
     * @author Mehdi Boulaid (358117)
     * @author Adnane Jamil (356117)
     */
    public record Tile(int id, Kind kind, TileSide n, TileSide e, TileSide s, TileSide w) {

        /**
         * Énumère les types de tuile possibles.
         *
         * {@code START, NORMAL, MENHIR}
         */
        public enum Kind {
            START,
            NORMAL,
            MENHIR
        }

        /**
         * Retourne les côtés de la tuile.
         *
         * @return La liste des côtés de la tuile.
         */
        public List<TileSide> sides() {
            return List.of(n, e, s, w);
        }

        /**
         * Retourne l'ensemble des zones associées aux côtés de la tuile.
         *
         * @return L'ensemble des zones associées aux côtés de la tuile.
         */
        public Set<Zone> sideZones() {
            Set<Zone> mysideZones = new HashSet<>();
            mysideZones.addAll(n.zones());
            mysideZones.addAll(e.zones());
            mysideZones.addAll(s.zones());
            mysideZones.addAll(w.zones());

            return Set.copyOf(mysideZones);

        }

        /**
         * Retourne l'ensemble de toutes les zones présentes sur la tuile, y compris les lacs associés aux rivières.
         *
         * @return L'ensemble de toutes les zones présentes sur la tuile.
         */
        public Set<Zone> zones() {
            Set<Zone> nzone = new HashSet<>();
            nzone.addAll(sideZones());

            for (Zone z : sideZones()) {
                if (z instanceof Zone.River river) {
                    if (river.hasLake()) {
                        nzone.add(river.lake());
                    }
                }
            }

            return nzone;
        }

    }
