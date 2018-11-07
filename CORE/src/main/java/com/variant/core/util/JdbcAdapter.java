package com.variant.core.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * @author Igor
 *
 */
public class JdbcAdapter {
	
	/**
	 * SQLException has its own stacking paradigm and log4j cannot unwind it.
	 * @param e
	 */
	private static RuntimeException toRuntimeException(SQLException e) {

		// Get all exceptions.
		ArrayList<SQLException> stack = new ArrayList<SQLException>();
		SQLException next = e.getNextException();
		while (next != null) {
			stack.add(next);
			next = next.getNextException();
		};
		
		// Build one long message.
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < stack.size(); i++) {
			msg.append(stack.get(i).getMessage());
		}
		
		return new RuntimeException(e.getMessage(), new RuntimeException(msg.toString()));
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

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
		catch (SQLException e) {
			throw toRuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.rollback();
//				conn.close();
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
		catch (SQLException e) {
			throw toRuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.rollback();
//				conn.close();
			}
		}
	}
	
}
