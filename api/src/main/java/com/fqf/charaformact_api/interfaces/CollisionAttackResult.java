package com.fqf.charaformact_api.interfaces;

public enum CollisionAttackResult {
	MOUNT,
	PAINFUL,
	NORMAL,
	GLANCING,
	RESISTED,
	FAIL;

	public enum ExecutableResult {
		MOUNT,
		PAINFUL,
		NORMAL,
		GLANCING,
		RESISTED
	}
}
