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