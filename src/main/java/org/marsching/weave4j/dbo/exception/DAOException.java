package org.marsching.weave4j.dbo.exception;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 24.01.2010
 * Time: 17:05:21
 * To change this template use File | Settings | File Templates.
 */
public class DAOException extends RuntimeException {
    /**
	 * Serial version UID. Generated automatically.
	 */
	private static final long serialVersionUID = 6625286005180785204L;

	public DAOException() {
    }

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }
}
