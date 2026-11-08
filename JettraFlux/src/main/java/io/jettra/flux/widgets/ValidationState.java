package io.jettra.flux.widgets;

/**
 * Idiomatic Java 25 sealed hierarchy representing the validation status of a form field or entity.
 */
public sealed interface ValidationState permits
    ValidationState.Valid,
    ValidationState.Invalid,
    ValidationState.Warning,
    ValidationState.Pending,
    ValidationState.None {

    record Valid(String message) implements ValidationState {
        public Valid() { this(""); }
    }

    record Invalid(String message) implements ValidationState {}

    record Warning(String message) implements ValidationState {}

    record Pending(String message) implements ValidationState {
        public Pending() { this("Verificando..."); }
    }

    record None() implements ValidationState {}

    static ValidationState valid() { return new Valid(); }
    static ValidationState valid(String message) { return new Valid(message); }
    static ValidationState invalid(String message) { return new Invalid(message); }
    static ValidationState warning(String message) { return new Warning(message); }
    static ValidationState pending() { return new Pending(); }
    static ValidationState pending(String message) { return new Pending(message); }
    static ValidationState none() { return new None(); }

    default boolean isValid() { return this instanceof Valid; }
    default boolean isInvalid() { return this instanceof Invalid; }
    default boolean isWarning() { return this instanceof Warning; }
    default boolean isPending() { return this instanceof Pending; }
    default boolean isNone() { return this instanceof None; }

    default String message() {
        return switch (this) {
            case Valid v -> v.message();
            case Invalid inv -> inv.message();
            case Warning w -> w.message();
            case Pending p -> p.message();
            case None ignored -> "";
        };
    }
}
