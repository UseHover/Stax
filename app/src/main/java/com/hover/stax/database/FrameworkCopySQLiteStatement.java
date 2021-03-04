package com.hover.stax.database;

import android.database.sqlite.SQLiteStatement;

import androidx.sqlite.db.SupportSQLiteStatement;

class FrameworkCopySQLiteStatement extends FrameworkCopySQLiteProgram implements SupportSQLiteStatement {
	private final SQLiteStatement mDelegate;

	/**
	 * Creates a wrapper around a framework {@link SQLiteStatement}.
	 *
	 * @param delegate The SQLiteStatement to delegate calls to.
	 */
	FrameworkCopySQLiteStatement(SQLiteStatement delegate) {
		super(delegate);
		mDelegate = delegate;
	}

	@Override
	public void execute() {
		mDelegate.execute();
	}

	@Override
	public int executeUpdateDelete() {
		return mDelegate.executeUpdateDelete();
	}

	@Override
	public long executeInsert() {
		return mDelegate.executeInsert();
	}

	@Override
	public long simpleQueryForLong() {
		return mDelegate.simpleQueryForLong();
	}

	@Override
	public String simpleQueryForString() {
		return mDelegate.simpleQueryForString();
	}
}