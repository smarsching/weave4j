/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010-2011  Sebastian Marsching
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marsching.weave4j.dbo.exception;

/**
 * Exception thrown by DAO layer, when the name of a non-existing user
 * is passed to a method.
 * 
 * @author Sebastian Marsching
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
