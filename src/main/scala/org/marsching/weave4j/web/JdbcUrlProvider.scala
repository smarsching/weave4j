/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010  Sebastian Marsching
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
package org.marsching.weave4j.web

import org.springframework.core.io.Resource

/**
 * Factory bean class for generating a HSQL JDBC URL, that places the database within the applications
 * WEB-INF directory.
 *
 * @author Sebastian Marsching
 */

class JdbcUrlProvider {

  private var resource: Resource = null

  /**
   * Sets the database location. This location will be used to generate the JDBC URL.
   *
   * @param resoure the resource pointing to the location, where the database shall be stored
   */
  def setDatabaseLocation(resource: Resource) {
    this.resource = resource
  }

  /**
   * Returns the JDBC URL for the database.
   *
   * @return HSQLDB JDBC URL
   */
  def getJdbcUrl: String = {
    if (resource == null) {
      throw new IllegalStateException("databaseLocation has to be set before calling this method")
    }

    val file = resource.getFile()
    if (file == null) {
      throw new IllegalStateException("Resource does not support resolution to a file")
    }

    return "jdbc:hsqldb:file:" + file.getAbsolutePath()
  }
}