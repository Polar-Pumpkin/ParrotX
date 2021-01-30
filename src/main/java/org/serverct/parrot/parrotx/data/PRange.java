package org.serverct.parrot.parrotx.data;

import com.google.common.collect.Range;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
@Data
@Builder
public class PRange<C extends Comparable> {

    private C max;
    private C min;
    private BiFunction<C, C, Integer> offsetFunction;

    public PRange(@NotNull C max, @NotNull C min, @NotNull BiFunction<C, C, Integer> offsetFunction) {
        this.max = max;
        this.min = min;
        this.offsetFunction = offsetFunction;
        swap();
    }

    public static <T, C extends Comparable> PRange<C> of(@NotNull T value1,
                                                         @NotNull T value2,
                                                         @NotNull Function<T, C> converter,
                                                         @NotNull BiFunction<C, C, Integer> offsetFunction) {
        return new PRange<>(converter.apply(value1), converter.apply(value2), offsetFunction);
    }

    public static <C extends Comparable> PRange<C> ofString(@NotNull String value,
                                                            @NotNull String symbol,
                                                            @NotNull Function<String, C> converter,
                                                            @NotNull BiFunction<C, C, Integer> offsetFunction) {
        final String[] values = value.split("[" + symbol + "]");
        if (values.length < 2) {
            final C onlyValue = converter.apply(value);
            return new PRange<>(onlyValue, onlyValue, offsetFunction);
        }
        return PRange.of(values[0], values[1], converter, offsetFunction);
    }

    public static <C extends Comparable> PRange<C> ofString(@NotNull String value,
                                                            @NotNull Function<String, C> converter,
                                                            @NotNull BiFunction<C, C, Integer> offsetFunction) {
        return PRange.ofString(value, "-", converter, offsetFunction);
    }

    public void update(final C value, final boolean upgrade) {
        swap(); // 先检查一下当前的最大最小值是否错位
        if (value.compareTo(max) > 0) { // 大于最大值
            if (!upgrade) { // 如果不要求升级
                min = max; // 收缩范围
            }
            max = value;
            return;
        }
        if (value.compareTo(min) < 0) { // 小于最小值
            if (!upgrade) { // 如果不要求升级
                max = min; // 收缩范围
            }
            min = value;
            return;
        }
        if (value.compareTo(max) < 0 && value.compareTo(min) > 0) { // 小于最大值且大于最小值
            if (upgrade) { // 如果要求升级
                min = value; // 那么提升最小值
            } else {
                max = value; // 否则降低最大值
            }
        }
    }

    public void swap() {
        if (max.compareTo(min) < 0) {
            final C temp = max;
            max = min;
            min = temp;
        }
    }

    public void setMax(C max) {
        this.max = max;
        swap();
    }

    public void setMin(C min) {
        this.min = min;
        swap();
    }

    public boolean inRange(final C value) {
        swap();
        return max.compareTo(value) <= 0 && min.compareTo(value) >= 0;
    }

    @NotNull
    public C random(final BiFunction<C, Integer, C> method) {
        swap();
        final int offset = Math.abs(offsetFunction.apply(max, min));
        return method.apply(min, offset == 0 ? 0 : new Random().nextInt(offset));
    }

    @NotNull
    public Range<C> toRange(final BiFunction<C, C, Range<C>> constructor) {
        swap();
        return constructor.apply(min, max);
    }

    @NotNull
    public String describe(final String symbol) {
        return min + symbol + max;
    }
}
