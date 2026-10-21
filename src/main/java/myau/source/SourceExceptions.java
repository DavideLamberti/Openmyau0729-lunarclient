package myau.source;

public final class SourceExceptions {
   private SourceExceptions() {
   }

   public static RuntimeException propagate(Throwable failure) {
      return throwOriginal(failure);
   }

   private static <Failure extends Throwable> RuntimeException throwOriginal(Throwable failure) throws Failure {
      throw (Failure)failure;
   }
}
