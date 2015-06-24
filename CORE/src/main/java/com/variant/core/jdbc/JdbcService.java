package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author Igor
 *
 */
public class JdbcService {

	/**
	 * A Query JDBC operation.
	 * @author Igor
	 * @param <T>
	 */
	public static interface QueryOperation<T> {
		T execute(Connection conn) throws SQLException;
	}

	/**
	 * An update JDBC operation.
	 * @author Igor
	 */
	public static interface UpdateOperation {
		void execute(Connection conn) throws SQLException;
	}

	/**
	 * Execute a query that returns an instance of type T
	 * @param conn
	 * @param op
	 * @return result of operation
	 */
	public static <T> T executeQuery(Connection conn, QueryOperation<T> op) throws SQLException {
	
		try {
			conn.setAutoCommit(false);
			T result = op.execute(conn);
			conn.commit();
			return result;
		}
		finally {
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}
		
	/**
	 * Execute an update operation that does not return anything.
	 * @param conn
	 * @param op
	 */
	public static void executeUpdate(Connection conn, UpdateOperation op) throws SQLException {
		try {
			conn.setAutoCommit(false);
			op.execute(conn);
			conn.commit();
		}
		finally {
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}	

}
