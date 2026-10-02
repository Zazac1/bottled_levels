package fr.zazac1.bottledlevels;

/** Pure, server-independent rules for transferring whole experience levels. */
public final class LevelTransferService {
    private LevelTransferService() {
    }

    public static Transfer deposit(int playerLevels, int storedLevels, int capacity) {
        if (!isValidNonNegative(playerLevels) || !isValidNonNegative(storedLevels) || capacity < 1) {
            return Transfer.rejected(playerLevels, storedLevels);
        }

        // A bottle may legitimately contain more than a later-lowered capacity.
        int space = storedLevels >= capacity ? 0 : capacity - storedLevels;
        int moved = Math.min(playerLevels, space);
        return new Transfer(playerLevels - moved, storedLevels + moved, moved, true);
    }

    public static Transfer withdraw(int playerLevels, int storedLevels) {
        if (!isValidNonNegative(playerLevels) || !isValidNonNegative(storedLevels)
                || storedLevels > Integer.MAX_VALUE - playerLevels) {
            return Transfer.rejected(playerLevels, storedLevels);
        }
        return new Transfer(playerLevels + storedLevels, 0, storedLevels, true);
    }

    private static boolean isValidNonNegative(int value) {
        return value >= 0;
    }

    public record Transfer(int playerLevelsAfter, int storedLevelsAfter, int movedLevels, boolean valid) {
        static Transfer rejected(int playerLevels, int storedLevels) {
            return new Transfer(playerLevels, storedLevels, 0, false);
        }

        public boolean changed() {
            return valid && movedLevels > 0;
        }
    }
}
