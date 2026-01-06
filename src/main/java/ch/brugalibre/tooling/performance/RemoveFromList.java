package ch.brugalibre.tooling.performance;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RemoveFromList {

    public static void main(String[] args) {
        testRemoveElements(5_000_000, 1_160_000);
        testRemoveElements(5_000_000, 3_160_000);
        testRemoveElements(5_000_000, 5_000_000);
        testRemoveElements(15_000_000, 5_660_000);
        testRemoveElements(15_000_000, 14_660_000);
    }

    private static void testRemoveElements(int totalSize, int elementsToRemove) {
        List<BigInteger> allIds = getRandomIds(totalSize);
        Collection<BigInteger> idsToRemove = getRandomIds(elementsToRemove);
        Map<Integer, String> idToValueMap = allIds.stream()
                .collect(Collectors.toMap(BigInteger::intValue, id -> "value" + id));
        
        // With Set
        long start = System.currentTimeMillis();
        Set<BigInteger> idsToRemove1 = new HashSet<>(idsToRemove);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to create HashSet from " + idsToRemove.size() + " IDs: " + (end - start) + "ms");
        collectValues(idToValueMap, idsToRemove1, allIds, "HashSet");

        // With List
//        collectValues(idToValueMap, new ArrayList<>(idsToRemove), allIds, "ArrayList");
    }

    private static void collectValues(Map<Integer, String> idToValueMap, Collection<BigInteger> idsToRemove,
                                      List<BigInteger> allIds, String method) {
        List<String> result = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (Map.Entry<Integer, String> idToValueEntry : idToValueMap.entrySet()) {
            if (!idsToRemove.contains(BigInteger.valueOf(idToValueEntry.getKey()))) {
                result.add(idToValueEntry.getValue());
            }
        }
        long end = System.currentTimeMillis();
        System.err.println(method + ": Time taken to remove " + idsToRemove.size() + " IDs from " + allIds.size() 
                + ", results: " + result.size() + ", Duration: " + (end - start) + "ms");
    }

    private static List<BigInteger> getRandomIds(int size) {
        List<BigInteger> ids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ids.add(BigInteger.valueOf(i));
        }
        return ids;
    }
}
