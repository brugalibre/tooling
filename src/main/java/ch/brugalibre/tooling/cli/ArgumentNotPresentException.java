package ch.brugalibre.tooling.cli;

public class ArgumentNotPresentException extends IllegalStateException {
    public ArgumentNotPresentException(String msg) {
        super(msg);
    }
}
