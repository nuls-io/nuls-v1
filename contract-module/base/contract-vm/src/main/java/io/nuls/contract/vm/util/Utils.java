package io.nuls.contract.vm.util;

public class Utils {

    public static int arrayListInitialCapacity(int size) {
        return Math.max(size, 10);
    }

    public static int hashMapInitialCapacity(int size) {
        return Math.max((int) (size / 0.75) + 1, 16);
    }

}
