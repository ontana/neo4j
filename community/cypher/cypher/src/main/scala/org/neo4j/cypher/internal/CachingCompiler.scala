/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal

import org.neo4j.cypher.SyntaxException
import org.neo4j.cypher.internal.compatibility.LFUCache

/**
  * Cypher compiler which has the ability to cache parsing and planning results.
  */
trait CachingCompiler[PARSED_QUERY <: AnyRef] extends Compiler {

  def parserCacheSize: Int
  def plannerCacheSize: Int

  private val parsedQueries = new LFUCache[String, PARSED_QUERY](parserCacheSize)

  @throws(classOf[SyntaxException])
  protected def getOrParse(preParsedQuery: PreParsedQuery,
                           parser: => Parser[PARSED_QUERY]
                     ): PARSED_QUERY = {
    parsedQueries.get(preParsedQuery.statementWithVersionAndPlanner).getOrElse {
      val parsedQuery = parser.parse(preParsedQuery)
      parsedQueries.put(preParsedQuery.statementWithVersionAndPlanner, parsedQuery)
      parsedQuery
    }
  }
}

trait Parser[PARSED_QUERY] {
  def parse(preParsedQuery: PreParsedQuery): PARSED_QUERY
}
