    package ch.epfl.chacun;

      public final class Points {

          /**
           * Calcule le score pour une forêt fermée en fonction du nombre de tuiles et du nombre de groupes de champignons.
           *
           * @param tileCount Le nombre de tuiles dans la forêt fermée.
           * @param mushroomGroupCount Le nombre de groupes de champignons dans la forêt fermée.
           * @return Le score calculé pour la forêt fermée.
           * @throws IllegalArgumentException si le nombre de tuiles est inférieur ou égal à 2 ou si le nombre de groupes de champignons est négatif.
           */
         public static int forClosedForest(int tileCount, int mushroomGroupCount){
            Preconditions.checkArgument(tileCount>1 && mushroomGroupCount>=0);
             return (tileCount*2 + mushroomGroupCount*3);
         }


          /**
           * Calcule le score pour une rivière fermée en fonction du nombre de tuiles et du nombre de poissons.
           *
           * @param tileCount Le nombre de tuiles dans la rivière fermée.
           * @param fishCount Le nombre de poissons dans la rivière fermée.
           * @return Le score calculé pour la rivière fermée.
           * @throws IllegalArgumentException si le nombre de tuiles est inférieur ou égale à 2 ou si le nombre de poissons est négatif.
           */
         public static int forClosedRiver(int tileCount,int fishCount){
             Preconditions.checkArgument(tileCount>1 && fishCount>=0);
             return tileCount+fishCount;
         }


          /**
           * Calcule le score pour une prairie en fonction du nombre de mammouths, d'aurochs et de cerfs.
           *
           * @param mammothCount Le nombre de mammouths dans la prairie.
           * @param auroschCount Le nombre d'aurochs dans la prairie.
           * @param deerCount Le nombre de cerfs dans la prairie.
           * @return Le score calculé pour la prairie.
           * @throws IllegalArgumentException si le nombre de mammouths, d'aurochs ou de cerfs est négatif.
           */
         public static int forMeadow(int mammothCount,int auroschCount, int deerCount){
             Preconditions.checkArgument(mammothCount>=0 && auroschCount>=0 && deerCount>=0 );
             return 3*mammothCount + 2*auroschCount + deerCount;
         }


          /**
           * Calcule le score pour un système de rivière en fonction du nombre de poissons.
           *
           * @param fishCount Le nombre de poissons dans le système de rivière.
           * @return Le score calculé pour le système de rivière.
           * @throws IllegalArgumentException si le nombre de poissons est négatif.
           */
         public static int forRiverSystem(int fishCount){
             Preconditions.checkArgument(fishCount>=0);
             return fishCount;
         }


          /**
           * Calcule le score pour un radeau en fonction du nombre de lacs.
           *
           * @param lakeCount Le nombre de lacs utilisés pour construire le radeau.
           * @return Le score calculé pour le radeau.
           * @throws IllegalArgumentException si le nombre de lacs est inférieur ou égal à 0.
           */
         public static int forLogboat(int lakeCount){
             Preconditions.checkArgument(lakeCount>0);
             return 2*lakeCount;
         }


          /**
           * Calcule le score pour un radeau en fonction du nombre de lacs.
           *
           * @param lakeCount Le nombre de lacs utilisés pour construire le radeau.
           * @return Le score calculé pour le radeau.
           * @throws IllegalArgumentException si le nombre de lacs est inférieur ou égal à 0.
           */
         public static int forRaft(int lakeCount){
             Preconditions.checkArgument(lakeCount>0);
             return lakeCount;
         }




    }
