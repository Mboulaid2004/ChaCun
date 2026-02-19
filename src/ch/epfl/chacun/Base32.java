package ch.epfl.chacun;

/**
 * A pour but d'encoder et de décoder des valeurs binaires en base32.
 *
 *@author Mehdi Boulaid (358117)
 * @author Adnane Jamil (356117)
 */
public final class Base32 {

    /**
     * Une chaîne contenant les caractères correspondant aux chiffres en base 32, ordonnés par poids croissant.
     */
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int ENCODED_VALUE = 0b11111;

    private Base32() {}

    /**
     * Valide une chaine de caractères.
     *
     * @param str la chaine de caractères à valider.
     * @return Vrai si et seulement si elle n'est composée que de caractères de l'alphabet base32
     */
    public static boolean isValid(String str) {
        return str.toUpperCase().chars().allMatch(c -> ALPHABET.indexOf(c) != -1);
    }

    /**
     * Encode une valeur representant 5 bits (0-31) en un seul caractère en base32.
     *
     * @param value La valeur à encoder.
     * @return L'encodage en base32 des 5 bits de poids faible de cette valeur.
     * @throws IllegalArgumentException si l'entier n'est pas compris entre 0 et 31.
     */
    public static String encodeBits5(int value) {
        Preconditions.checkArgument(value >= 0);
        return String.valueOf(ALPHABET.charAt(value % 32));
    }

    /**
     * Encode une valeur representant 10 bits en un une chaine de longueur 3 en base32.
     *
     * @param value La valeur à encoder.
     * @return L'encodage en base32 des 10 bits de poids faible de cette valeur.
     * @throws IllegalArgumentException si l'entier n'est pas compris entre 0 et 1022
     */
    public static String encodeBits10(int value) {
        Preconditions.checkArgument(value >= 0 && value < 1023);
        int high = (value >> 5) & ENCODED_VALUE; // Extract the high 5 bits
        int low = value & ENCODED_VALUE;         // Extract the low 5 bits
        return STR."\{ALPHABET.charAt(high)}\{ALPHABET.charAt(low)}";
    }

    /**
     * Decode une chaine de caractères en base32 de longueur 1 ou 2 en un entier.
     *
     * @param base32 Une chaîne de longueur 1 ou 2 représentant un nombre en base32.
     * @return L'entier décodé.
     * @throws IllegalArgumentException si le string est nul ou de taille supérieure à 2.
     */
    public static int decode(String base32) {
        Preconditions.checkArgument(base32 != null && !base32.isEmpty() && base32.length() <= 2);
        base32 = base32.toUpperCase();
        int value = 0;
        for (int i = 0; i < base32.length(); i++) {
            int digit = ALPHABET.indexOf(base32.charAt(i));
            if (digit == -1) {
                throw new IllegalArgumentException(STR."Invalid Base32 character: \{base32.charAt(i)}");
            }
            value = (value << 5) + digit;
        }
        return value;
    }
}