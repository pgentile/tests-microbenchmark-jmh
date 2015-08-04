package microbenchmark;

import example.concurrent.CompletableFutures;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// @BenchmarkMode(Mode.AverageTime)
public class MergeCompletableFutureBenchmarks {

    @State(Scope.Thread)
    public static class BenchmarkState {

        public List<CompletableFuture<Long>> inputs;

        private ExecutorService executor;

        @Setup
        public void setup() {
            executor = Executors.newCachedThreadPool();

            final int size = 100;
            inputs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final long number = i;
                inputs.add(CompletableFuture.supplyAsync(() -> number, executor));
            }
        }

        @TearDown
        public void tearDown() {
            executor.shutdownNow();
        }

    }

    @Benchmark
    public CompletableFuture<List<Long>> mergeWithAllOf(BenchmarkState state) {
        return CompletableFutures.mergeAllOf(state.inputs);
    }

    @Benchmark
    public CompletableFuture<List<Long>> mergeWithAllOfForEach(BenchmarkState state) {
        return CompletableFutures.mergeAllOfForEach(state.inputs);
    }

    @Benchmark
    public CompletableFuture<List<Long>> mergeWithCombine(BenchmarkState state) {
        return CompletableFutures.mergeCombine(state.inputs);
    }

}
