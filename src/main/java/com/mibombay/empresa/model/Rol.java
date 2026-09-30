package com.mibombay.empresa.model;

public enum Rol {

	ADMIN,
	CAJERO,
	DEV;

	/**
	 * Devuelve el nombre con el prefijo que usa Spring Security.
	 */
	public String getPrefijo() {
		return "ROLE_" + name();
	}

}
