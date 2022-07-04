package xyz.destiall.caramel.api.interfaces;

import java.io.Serializable;

public interface StringWrapper extends CharSequence, Comparable<String>, Serializable {
    void set(String s);
    String get();
    default void append(String s) {
        set(get() + s);
    }

    default void insert(int index, String s) {
        set(new StringBuilder(get()).insert(index, s).toString());
    }

    default int indexOf(String s) {
        return get().indexOf(s);
    }

    default int indexOf(int start, String s) {
        return get().indexOf(s, start);
    }

    default char charAt(int index) {
        return get().charAt(index);
    }

    default String substring(int start) {
        return substring(start, length());
    }

    default String substring(int start, int end) {
        return get().substring(start, end);
    }

    default CharSequence subSequence(int start, int end) {
        return get().subSequence(start, end);
    }

    default int length() {
        return get().length();
    }

    default int compareTo(String o) {
        return get().compareTo(o);
    }

    default String toLowerCase() {
        return get().toLowerCase();
    }

    default String toUpperCase() {
        return get().toUpperCase();
    }

    default String trim() {
        return get().trim();
    }

    default String[] split(String regex) {
        return get().split(regex);
    }

    default String[] split(String regex, int limit) {
        return get().split(regex, limit);
    }

    default String replace(CharSequence replace, CharSequence target) {
        return get().replace(replace, target);
    }

    default String replaceAll(String regex, String replace) {
        return get().replaceAll(regex, replace);
    }

    default boolean isEmpty() {
        return get().isEmpty();
    }

    default boolean contains(CharSequence substring) {
        return get().contains(substring);
    }

    default boolean equalsIgnoreCase(String anotherString) {
        return get().equalsIgnoreCase(anotherString);
    }

    default boolean endsWith(String suffix) {
        return get().endsWith(suffix);
    }

    default boolean startsWith(String prefix) {
        return get().startsWith(prefix);
    }

    default byte[] getBytes() {
        return get().getBytes();
    }
}
