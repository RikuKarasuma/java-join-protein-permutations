package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class ProteinJoiner {

    // Pojo
    private record Proteins(String id, int start, int end) {}

    public static void main(String[] args) {
        final var testData = Arrays.asList(
                new Proteins("P1", 1, 34),
                new Proteins("P2", 3, 10),
                new Proteins("P3", 34, 43),
                new Proteins("P4", 10, 15),
                new Proteins("P5", 15, 26),
                new Proteins("P6", 43, 65),
                new Proteins("P7", 65, 89),
                new Proteins("P8", 15, 20),
                new Proteins("P9", 20, 26),
                new Proteins("P10", 65, 89)
        );

        final var expectedCombos = 27;

        // For a truly accurate comparison, run 1 or the other.
        // Running both will skewer results, possibly due to
        // JVM optimization.

        final var timeThen = System.currentTimeMillis();
        final var distinctCombos = findDistinctCombos(testData);
        final var timeItTook = System.currentTimeMillis() - timeThen;
        assert distinctCombos.size() == expectedCombos;

        System.out.println("Time: " + (timeItTook) +"ms   # of Combos: " + distinctCombos.size());
        for (final Proteins combo : distinctCombos)
            System.out.println("  id:  " + combo.id +
                    "  start:  " + combo.start +
                    "  end:  " + combo.end);

//        final var timeThenMapVersion = System.currentTimeMillis();
//        final var distinctCombosMapVersion = findDistinctCombosMapVersion(testData);
//        final var timeItTookMapVersion = System.currentTimeMillis() - timeThenMapVersion;
//        assert distinctCombosMapVersion.size() == expectedCombos;
//
//        System.out.println("Time: " + (timeItTookMapVersion) +"ms   # of Combos: " + distinctCombosMapVersion.size());
//        for (final Proteins combo : distinctCombosMapVersion)
//            System.out.println("  id:  " + combo.id +
//                    "  start:  " + combo.start +
//                    "  end:  " + combo.end);
    }

    // Less elegant, produces duplicates, more efficient.
    public static List<Proteins> findDistinctCombos(final List<Proteins> proteins) {
        // Add original set to combine with.
        final List<Proteins> longestCompatibleJoins = new ArrayList<>(proteins) {
            @Override
            public boolean add(final Proteins potentialDup) {
                // Make sure it isn't a duplicate.
                return this.stream().noneMatch(existing -> existing.id.equals(potentialDup.id)) &&
                        super.add(potentialDup);
            }
        };

        // Iterate over all possible combos
        for (int i = 0; i < longestCompatibleJoins.size(); i ++)
            for (int x = 0; x < longestCompatibleJoins.size(); x ++)
                // Finding start -> end index matches.
                if (longestCompatibleJoins.get(x).start == longestCompatibleJoins.get(i).end)
                    longestCompatibleJoins.add(new Proteins(longestCompatibleJoins.get(i).id + "+" + longestCompatibleJoins.get(x).id,
                            longestCompatibleJoins.get(i).start, longestCompatibleJoins.get(x).end));

        return longestCompatibleJoins;
    }

    // More elegant, less efficient.
    public static List<Proteins> findDistinctCombosMapVersion(final List<Proteins> proteins) {

        // Group proteins by their start index.
        final var proteinsKeyedByStart = proteins.stream()
                .collect(Collectors.groupingBy(Proteins::start));

        // Create return collection.
        final var toReturn = new ArrayList<>(proteins);

        // Iterate over existing and new combinations.
        for ( int i = 0; i < toReturn.size(); i ++) {
            final var currentProtein = toReturn.get(i);
            final var proteinsWhichStartAtThisEnd = proteinsKeyedByStart.get(currentProtein.end);
            // If we have no groups which exist for this start index...
            // continue looping.
            if (Objects.isNull(proteinsWhichStartAtThisEnd))
                continue;

            // Create new protein variation and add to return list.
            final var newVariations = proteinsWhichStartAtThisEnd.stream()
                    .map(proteinToJoin -> new Proteins(currentProtein.id + "_" + proteinToJoin.id,
                            currentProtein.start, proteinToJoin.end)).toList();

            toReturn.addAll(newVariations);
        }

        return toReturn;
    }
}
