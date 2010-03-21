package org.marsching.weave4j.dbo.exception;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 24.01.2010
 * Time: 17:06:54
 * To change this template use File | Settings | File Templates.
 */
public class InvalidUserException extends DAOException {
    /**
	 * Serial version UID. Generated automatically.
	 */
	private static final long serialVersionUID = -7537218497096385829L;

	public InvalidUserException() {
    }

    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUserException(Throwable cause) {
        super(cause);
    }
}
