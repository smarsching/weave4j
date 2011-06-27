/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2011  Sebastian Marsching
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

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.TransactionDefinition

/**
 * Wrapper around a {@link PlatformTransactionManager}.
 */
class TransactionManager {
  /**
   * Spring platform transaction manager for managing database transactions.
   */
  protected var platformTransactionManager: PlatformTransactionManager = null

  /**
   * Sets the Spring platform transaction manager, used to manager transactions.
   *
   * @param platformTransactionManager transaction manager
   */
  def setPlatformTransactionManager(platformTransactionManager: PlatformTransactionManager) = {
    this.platformTransactionManager = platformTransactionManager
  }

  /**
   * Performs an action within a database transaction.
   *
   * @param readOnly if set to <code>true</code>, a read-only transaction will be started
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  protected def withTransaction[T](readOnly: Boolean)(f: => T): T = {
    val transactionDefinition = new DefaultTransactionDefinition()
    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER)
    transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE)
    transactionDefinition.setReadOnly(readOnly)
    val transactionStatus = platformTransactionManager.getTransaction(transactionDefinition)
    try {
      val result: T = f
      platformTransactionManager.commit(transactionStatus)
      return result
    } finally {
      if (!transactionStatus.isCompleted()) {
        platformTransactionManager.rollback(transactionStatus)
      }
    }
  }

  /**
   * Performs an action within a read/write database transaction.
   *
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  def withReadWriteTransaction[T](f: => T): T = {
    withTransaction(false)(f)
  }

  /**
   * Performs an action within a read-only database transaction.
   *
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  def withReadOnlyTransaction[T](f: => T): T = {
    withTransaction(true)(f)
  }

}
