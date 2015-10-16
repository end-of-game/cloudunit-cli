package fr.treeptik.cloudunit.cli.exception;

/**
 * Created by guillaume on 16/10/15.
 */
public class ManagerResponseException extends Exception {

    public ManagerResponseException(String message, Throwable e) {
        super(message, e);
    }
}
