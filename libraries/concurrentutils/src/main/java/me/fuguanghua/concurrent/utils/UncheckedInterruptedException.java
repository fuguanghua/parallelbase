package me.fuguanghua.concurrent.utils;

/**
 * Represents an {@link InterruptedException} that has been caught and rethrown as a runtime (unchecked) exception.
 *
 */
public class UncheckedInterruptedException extends RuntimeException {
  /**
   * Creates a new instance, wrapped the provided {@link InterruptedException}
   *
   * @param e the {@link InterruptedException} being wrapped
   */
  public UncheckedInterruptedException(InterruptedException e) {
    super(e);
  }

  @Override
  public InterruptedException getCause() {
    return (InterruptedException) super.getCause();
  }
}
