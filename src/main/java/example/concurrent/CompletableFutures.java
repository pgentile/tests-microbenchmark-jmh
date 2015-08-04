package example.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CompletableFutures {

    private CompletableFutures() {
        // Ne rien faire
    }

    public static <T> CompletableFuture<List<T>> mergeAllOf(List<CompletableFuture<T>> futures) {
        final CompletableFuture<?>[] array = new CompletableFuture<?>[futures.size()];
        futures.toArray(array);

        final CompletableFuture<?> combinedFuture = CompletableFuture.allOf(array);

        return combinedFuture
                .thenApply(x -> {
                    return futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                })
                .thenApply(Collections::unmodifiableList);
    }

    public static <T> CompletableFuture<List<T>> mergeAllOfForEach(List<CompletableFuture<T>> futures) {
        final int size = futures.size();
        final CompletableFuture<?>[] array = new CompletableFuture<?>[size];
        futures.toArray(array);

        final CompletableFuture<?> combinedFuture = CompletableFuture.allOf(array);

        return combinedFuture
                .thenApply(x -> {
                    final List<T> merged = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        merged.add(futures.get(i).join());
                    }
                    return merged;
                })
                .thenApply(Collections::unmodifiableList);
    }

    public static <T> CompletableFuture<List<T>> mergeCombine(List<CompletableFuture<T>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final Iterator<CompletableFuture<T>> iterator = futures.iterator();

        CompletableFuture<List<T>> mergedFuture = iterator.next().thenApply(item -> {
            final List<T> items = new ArrayList<>(futures.size());
            items.add(item);
            return items;
        });

        while (iterator.hasNext()) {
            mergedFuture = mergedFuture.thenCombine(iterator.next(), (items, item) -> {
                items.add(item);
                return items;
            });
        }

        return mergedFuture.thenApply(Collections::unmodifiableList);
    }


}
