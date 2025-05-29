package main.java.com.realmwar.game.util;

public class CustomExceptions {

    public static class InsufficientResourcesException extends Exception {
        public InsufficientResourcesException(String message) {
            super(message);
        }
    }
    public static class InvalidPlacementException extends Exception {
        public InvalidPlacementException(String message) {
            super(message);
        }
    }
    public static class InsufficientSpaceException extends Exception {
        public InsufficientSpaceException(String message) {
            super(message);
        }
    }
    public static class InvalidActionException extends Exception {
        public InvalidActionException(String message) {
            super(message);
        }
    }
}



