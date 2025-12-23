package com.example.AzureTestProject.Api.Model;

public class Pairs<L, R> {
    private final L left;
    private final R right;

    private Pairs(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pairs<L, R> of(L left, R right) {
        return new Pairs<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pairs<?, ?> pair)) return false;
        return java.util.Objects.equals(left, pair.left) &&
                java.util.Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(left, right);
    }
}
