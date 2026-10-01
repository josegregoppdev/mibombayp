package com.mibombay.empresa.util;

/**
 * Validaciones de entrada reutilizables en todo el sistema.
 * Todos los métodos lanzan IllegalArgumentException con el nombre del campo
 * cuando el dato no es válido; si el dato es correcto, lo devuelven (recortado).
 */
public final class ValidacionDatos {

	private static final String[] PATRONES_PELIGROSOS = {
			"--", "/*", "*/", ";",
			"union select", "drop table", "insert into", "delete from",
			"update set", "select from", "xp_", "' or", "\" or",
			"<script", "sleep(", "benchmark("
	};

	private ValidacionDatos() {
	}

	// ------------------------------------------------------------------
	// Métodos genéricos: valen para cualquier entidad
	// ------------------------------------------------------------------

	// Un solo concepto "requerido": para Strings valida vacío y devuelve
	// recortado; para cualquier otro objeto (enum, Long, BigDecimal...)
	// solo valida que no sea null y lo devuelve tal cual.
	public static String requerido(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException(campo + ": no puede estar vacío");
		}
		return valor.trim();
	}

	public static Object requerido(Object valor, String campo) {
		if (valor == null) {
			throw new IllegalArgumentException(campo + ": no puede estar vacío");
		}
		return valor;
	}

	public static void longitudMaxima(String valor, int maximo, String campo) {
		if (valor != null && valor.length() > maximo) {
			throw new IllegalArgumentException(campo + ": máximo " + maximo + " caracteres");
		}
	}

	public static void longitudMinima(String valor, int minimo, String campo) {
		if (valor != null && valor.length() < minimo) {
			throw new IllegalArgumentException(campo + ": mínimo " + minimo + " caracteres");
		}
	}

	public static String sinInyeccion(String valor, String campo) {
		if (valor == null) {
			return null;
		}
		String enMinusculas = valor.toLowerCase();
		for (String patron : PATRONES_PELIGROSOS) {
			if (enMinusculas.contains(patron)) {
				throw new IllegalArgumentException(campo + ": contiene caracteres no permitidos");
			}
		}
		return valor;
	}

	// ------------------------------------------------------------------
	// Comodines del módulo de usuarios (usan los genéricos de arriba)
	// ------------------------------------------------------------------

	public static String username(String valor) {
		String limpio = requerido(valor, "El usuario");
		sinInyeccion(limpio, "El usuario");
		if (!limpio.matches("[A-Za-z0-9._-]+")) {
			throw new IllegalArgumentException("El usuario: solo letras, números, punto, guion y guion bajo");
		}
		longitudMaxima(limpio, 50, "El usuario");
		return limpio;
	}

	public static String nombre(String valor) {
		String limpio = requerido(valor, "El nombre");
		sinInyeccion(limpio, "El nombre");
		longitudMaxima(limpio, 100, "El nombre");
		return limpio;
	}

	public static String password(String valor) {
		String limpio = requerido(valor, "La contraseña");
		longitudMinima(limpio, 4, "La contraseña");
		return limpio;
	}

	public static java.math.BigDecimal stock(java.math.BigDecimal valor, String campo) {
		requerido(valor, campo);
		if (valor.signum() < 0) {
			throw new IllegalArgumentException(campo + ": no puede ser negativo");
		}
		return valor;
	}

}
