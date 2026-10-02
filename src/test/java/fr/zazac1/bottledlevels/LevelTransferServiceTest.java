package fr.zazac1.bottledlevels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTransferServiceTest {
    @Test
    void depositTransfersMaximumWholeLevels() {
        assertTransfer(LevelTransferService.deposit(22, 0, 30), 0, 22, 22);
        assertTransfer(LevelTransferService.deposit(47, 0, 30), 17, 30, 30);
        assertTransfer(LevelTransferService.deposit(22, 12, 30), 4, 30, 18);
    }

    @Test
    void depositDoesNothingForNoLevelsOrFullBottle() {
        assertTransfer(LevelTransferService.deposit(0, 0, 30), 0, 0, 0);
        assertTransfer(LevelTransferService.deposit(22, 30, 30), 22, 30, 0);
    }

    @Test
    void withdrawReturnsAllWholeLevelsIncludingAboveCurrentCapacity() {
        assertTransfer(LevelTransferService.withdraw(15, 22), 37, 0, 22);
        assertTransfer(LevelTransferService.withdraw(0, 22), 22, 0, 22);
        assertTransfer(LevelTransferService.withdraw(80, 22), 102, 0, 22);
        assertTransfer(LevelTransferService.withdraw(5, 30), 35, 0, 30);
    }

    @Test
    void roundTripsDoNotDriftAcrossOneThousandCycles() {
        int player = 22;
        int bottle = 0;
        for (int cycle = 0; cycle < 1_000; cycle++) {
            LevelTransferService.Transfer deposit = LevelTransferService.deposit(player, bottle, 30);
            player = deposit.playerLevelsAfter();
            bottle = deposit.storedLevelsAfter();
            LevelTransferService.Transfer withdraw = LevelTransferService.withdraw(player, bottle);
            player = withdraw.playerLevelsAfter();
            bottle = withdraw.storedLevelsAfter();
        }
        assertEquals(22, player);
        assertEquals(0, bottle);
    }

    @Test
    void rejectsInvalidAndOverflowingTransfersWithoutTruncation() {
        assertFalse(LevelTransferService.deposit(4, 0, 0).valid());
        assertFalse(LevelTransferService.withdraw(Integer.MAX_VALUE, 1).valid());
        assertTrue(LevelTransferService.withdraw(15, 0).valid());
    }

    private static void assertTransfer(LevelTransferService.Transfer transfer, int player, int bottle, int moved) {
        assertTrue(transfer.valid());
        assertEquals(player, transfer.playerLevelsAfter());
        assertEquals(bottle, transfer.storedLevelsAfter());
        assertEquals(moved, transfer.movedLevels());
    }
}
