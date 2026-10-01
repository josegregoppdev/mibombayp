package com.mibombay.empresa.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.mibombay.empresa.model.Rol;

class TestValidacionDatos {

	// ------------------------------------------------------------------
	// requerido con String
	// ------------------------------------------------------------------

	// Camino feliz: valor con espacios -> devuelve recortado (trim)
	@Test
	void requerido_valorValido_retornaRecortado() {
		// Given
		String valor = "  hola  ";
		String campo = "El nombre";

		// When
		String resultado = ValidacionDatos.requerido(valor, campo);

		// Then
		assertEquals("hola", resultado);
	}

	// Excepción: valor nulo -> IllegalArgumentException
	@Test
	void requerido_valorNulo_lanzaExcepcion() {
		// Given
		String valor = null;
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.requerido(valor, campo));

		// Then
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
	}

	// Excepción: valor vacío -> IllegalArgumentException
	@Test
	void requerido_valorVacio_lanzaExcepcion() {
		// Given
		String valor = "";
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.requerido(valor, campo));

		// Then
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
	}

	// Excepción: valor solo espacios (isBlank) -> IllegalArgumentException
	@Test
	void requerido_valorSoloEspacios_lanzaExcepcion() {
		// Given
		String valor = "   ";
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.requerido(valor, campo));

		// Then
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
	}

	// ------------------------------------------------------------------
	// requerido con objeto (enum, Long, BigDecimal...)
	// ------------------------------------------------------------------

	// Camino feliz: objeto no nulo -> devuelve el mismo objeto tal cual
	@Test
	void requerido_objetoNoNulo_retornaMismoObjeto() {
		// Given
		Rol valor = Rol.ADMIN;
		String campo = "El rol";

		// When
		Object resultado = ValidacionDatos.requerido(valor, campo);

		// Then
		assertEquals(Rol.ADMIN, resultado);
	}

	// Null/Excepción: objeto nulo -> IllegalArgumentException
	@Test
	void requerido_objetoNulo_lanzaExcepcion() {
		// Given
		Rol valor = null;
		String campo = "El rol";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.requerido(valor, campo));

		// Then
		assertEquals("El rol: no puede estar vacío", error.getMessage());
	}

	// ------------------------------------------------------------------
	// longitudMaxima
	// ------------------------------------------------------------------

	// Camino feliz: valor dentro del límite -> no lanza excepción
	@Test
	void longitudMaxima_valorDentroDelLimite_noLanzaExcepcion() {
		// Given
		String valor = "hola";
		int maximo = 10;
		String campo = "El nombre";

		// When
		ValidacionDatos.longitudMaxima(valor, maximo, campo);

		// Then
		// No lanza excepción: "hola" (4) cabe en el máximo de 10
	}

	// Camino feliz (límite exacto): valor igual al máximo -> no lanza excepción
	@Test
	void longitudMaxima_valorIgualAlMaximo_noLanzaExcepcion() {
		// Given
		String valor = "abcde";
		int maximo = 5;
		String campo = "El nombre";

		// When
		ValidacionDatos.longitudMaxima(valor, maximo, campo);

		// Then
		// No lanza excepción: la validación es SOLO por encima del máximo (>),
		// llegar exacto a 5 sí está permitido
	}

	// Null: valor nulo -> no valida nada, no lanza excepción (tolerante a null)
	@Test
	void longitudMaxima_valorNulo_noLanzaExcepcion() {
		// Given
		String valor = null;
		int maximo = 10;
		String campo = "El nombre";

		// When
		ValidacionDatos.longitudMaxima(valor, maximo, campo);

		// Then
		// No lanza excepción: el null se ignora aquí (quien exige no vacío es requerido)
	}

	// Excepción: valor que supera el máximo -> IllegalArgumentException
	@Test
	void longitudMaxima_valorSuperaElMaximo_lanzaExcepcion() {
		// Given
		String valor = "abcdef";
		int maximo = 3;
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.longitudMaxima(valor, maximo, campo));

		// Then
		assertEquals("El nombre: máximo 3 caracteres", error.getMessage());
	}

	// ------------------------------------------------------------------
	// longitudMinima
	// ------------------------------------------------------------------

	// Camino feliz: valor por encima del mínimo -> no lanza excepción
	@Test
	void longitudMinima_valorPorEncimaDelMinimo_noLanzaExcepcion() {
		// Given
		String valor = "hola";
		int minimo = 3;
		String campo = "La contraseña";

		// When
		ValidacionDatos.longitudMinima(valor, minimo, campo);

		// Then
		// No lanza excepción: "hola" (4) supera el mínimo de 3
	}

	// Camino feliz (límite exacto): valor igual al mínimo -> no lanza excepción
	@Test
	void longitudMinima_valorIgualAlMinimo_noLanzaExcepcion() {
		// Given
		String valor = "abc";
		int minimo = 3;
		String campo = "La contraseña";

		// When
		ValidacionDatos.longitudMinima(valor, minimo, campo);

		// Then
		// No lanza excepción: la validación es SOLO por debajo del mínimo (<),
		// llegar exacto a 3 sí está permitido
	}

	// Null: valor nulo -> no valida nada, no lanza excepción (tolerante a null)
	@Test
	void longitudMinima_valorNulo_noLanzaExcepcion() {
		// Given
		String valor = null;
		int minimo = 4;
		String campo = "La contraseña";

		// When
		ValidacionDatos.longitudMinima(valor, minimo, campo);

		// Then
		// No lanza excepción: el null se ignora aquí (quien exige no vacío es requerido)
	}

	// Excepción: valor más corto que el mínimo -> IllegalArgumentException
	@Test
	void longitudMinima_valorBajoElMinimo_lanzaExcepcion() {
		// Given
		String valor = "abc";
		int minimo = 4;
		String campo = "La contraseña";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.longitudMinima(valor, minimo, campo));

		// Then
		assertEquals("La contraseña: mínimo 4 caracteres", error.getMessage());
	}

	// ------------------------------------------------------------------
	// sinInyeccion
	// ------------------------------------------------------------------

	// Camino feliz: texto sin patrones peligrosos -> devuelve el valor igual
	@Test
	void sinInyeccion_textoSinPatrones_retornaValor() {
		// Given
		String valor = "Coca Cola 500ml";
		String campo = "El nombre";

		// When
		String resultado = ValidacionDatos.sinInyeccion(valor, campo);

		// Then
		assertEquals("Coca Cola 500ml", resultado);
	}

	// Null: valor nulo -> no valida nada, devuelve null (tolerante a null)
	@Test
	void sinInyeccion_valorNulo_retornaNull() {
		// Given
		String valor = null;
		String campo = "El nombre";

		// When
		String resultado = ValidacionDatos.sinInyeccion(valor, campo);

		// Then
		assertEquals(null, resultado);
	}

	// Excepción: patrón peligroso en mayúsculas -> IllegalArgumentException
	// (verifica que la comparación es insensible a mayúsculas: "DROP TABLE"
	// se pasa a minúsculas y matchea el patrón "drop table")
	@Test
	void sinInyeccion_dropTableEnMayusculas_lanzaExcepcion() {
		// Given
		String valor = "DROP TABLE usuario";
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.sinInyeccion(valor, campo));

		// Then
		assertEquals("El nombre: contiene caracteres no permitidos", error.getMessage());
	}

	// Excepción: punto y coma (patrón ";") -> IllegalArgumentException
	@Test
	void sinInyeccion_puntoYComa_lanzaExcepcion() {
		// Given
		String valor = "papas; con queso";
		String campo = "El nombre";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.sinInyeccion(valor, campo));

		// Then
		assertEquals("El nombre: contiene caracteres no permitidos", error.getMessage());
	}

	// ------------------------------------------------------------------
	// username (compuesto: requerido -> sinInyeccion -> regex -> longitudMaxima)
	// ------------------------------------------------------------------

	// Camino feliz: username válido con espacios alrededor -> devuelve recortado
	@Test
	void username_valorValido_retornaRecortado() {
		// Given
		String valor = "  admin.user_1  ";

		// When
		String resultado = ValidacionDatos.username(valor);

		// Then
		assertEquals("admin.user_1", resultado);
	}

	// Excepción (null): valor nulo -> falla en requerido (primera validación)
	@Test
	void username_valorNulo_lanzaExcepcion() {
		// Given
		String valor = null;

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.username(valor));

		// Then
		assertEquals("El usuario: no puede estar vacío", error.getMessage());
	}

	// Excepción (regex): carácter no permitido (espacio) -> IllegalArgumentException
	@Test
	void username_conEspacios_lanzaExcepcion() {
		// Given
		String valor = "con espacios";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.username(valor));

		// Then
		assertEquals("El usuario: solo letras, números, punto, guion y guion bajo", error.getMessage());
	}

	// Excepción (sinInyeccion): patrón peligroso -> se rechaza ANTES del regex,
	// con el mensaje de caracteres no permitidos
	@Test
	void username_patronPeligroso_lanzaExcepcion() {
		// Given
		String valor = "drop table";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.username(valor));

		// Then
		assertEquals("El usuario: contiene caracteres no permitidos", error.getMessage());
	}

	// Excepción (longitudMaxima): más de 50 caracteres válidos -> IllegalArgumentException
	@Test
	void username_masDe50Caracteres_lanzaExcepcion() {
		// Given
		String valor = "a".repeat(51);

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.username(valor));

		// Then
		assertEquals("El usuario: máximo 50 caracteres", error.getMessage());
	}

	// ------------------------------------------------------------------
	// nombre (compuesto: requerido -> sinInyeccion -> longitudMaxima)
	// ------------------------------------------------------------------

	// Camino feliz: nombre válido con espacios alrededor -> devuelve recortado
	@Test
	void nombre_valorValido_retornaRecortado() {
		// Given
		String valor = "  Coca Cola 500ml  ";

		// When
		String resultado = ValidacionDatos.nombre(valor);

		// Then
		assertEquals("Coca Cola 500ml", resultado);
	}

	// Excepción (null): valor nulo -> falla en requerido (primera validación)
	@Test
	void nombre_valorNulo_lanzaExcepcion() {
		// Given
		String valor = null;

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.nombre(valor));

		// Then
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
	}

	// Excepción (sinInyeccion): patrón peligroso -> IllegalArgumentException
	@Test
	void nombre_conPatronPeligroso_lanzaExcepcion() {
		// Given
		String valor = "papas; con queso";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.nombre(valor));

		// Then
		assertEquals("El nombre: contiene caracteres no permitidos", error.getMessage());
	}

	// Excepción (longitudMaxima): más de 100 caracteres -> IllegalArgumentException
	@Test
	void nombre_masDe100Caracteres_lanzaExcepcion() {
		// Given
		String valor = "a".repeat(101);

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.nombre(valor));

		// Then
		assertEquals("El nombre: máximo 100 caracteres", error.getMessage());
	}

	// ------------------------------------------------------------------
	// password (compuesto: requerido -> longitudMinima; SIN sinInyeccion:
	// la contraseña sí puede llevar caracteres especiales)
	// ------------------------------------------------------------------

	// Camino feliz (límite exacto): contraseña de 4 caracteres -> devuelve igual
	@Test
	void password_con4Caracteres_retornaValor() {
		// Given
		String valor = "1234";

		// When
		String resultado = ValidacionDatos.password(valor);

		// Then
		assertEquals("1234", resultado);
	}

	// Camino feliz: caracteres especiales permitidos -> devuelve igual
	@Test
	void password_conCaracteresEspeciales_retornaValor() {
		// Given
		String valor = "p@ss!";

		// When
		String resultado = ValidacionDatos.password(valor);

		// Then
		assertEquals("p@ss!", resultado);
	}

	// Excepción (null): valor nulo -> falla en requerido (primera validación)
	@Test
	void password_valorNulo_lanzaExcepcion() {
		// Given
		String valor = null;

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.password(valor));

		// Then
		assertEquals("La contraseña: no puede estar vacío", error.getMessage());
	}

	// Excepción (longitudMinima): menos de 4 caracteres -> IllegalArgumentException
	@Test
	void password_menosDe4Caracteres_lanzaExcepcion() {
		// Given
		String valor = "abc";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.password(valor));

		// Then
		assertEquals("La contraseña: mínimo 4 caracteres", error.getMessage());
	}

	// ------------------------------------------------------------------
	// stock (requerido + no negativo; para BigDecimal 12,3 / 12,2)
	// ------------------------------------------------------------------

	// Camino feliz: valor positivo -> devuelve el mismo valor
	@Test
	void stock_valorPositivo_retornaValor() {
		// Given
		BigDecimal valor = new BigDecimal("10.5");
		String campo = "El stock actual";

		// When
		BigDecimal resultado = ValidacionDatos.stock(valor, campo);

		// Then
		assertEquals(valor, resultado);
	}

	// Camino feliz (límite exacto): cero -> permitido (no es negativo)
	@Test
	void stock_valorCero_retornaValor() {
		// Given
		BigDecimal valor = BigDecimal.ZERO;
		String campo = "El stock mínimo";

		// When
		BigDecimal resultado = ValidacionDatos.stock(valor, campo);

		// Then
		assertEquals(BigDecimal.ZERO, resultado);
	}

	// Excepción (null): valor nulo -> falla en requerido (primera validación)
	@Test
	void stock_valorNulo_lanzaExcepcion() {
		// Given
		BigDecimal valor = null;
		String campo = "El stock actual";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.stock(valor, campo));

		// Then
		assertEquals("El stock actual: no puede estar vacío", error.getMessage());
	}

	// Excepción (negativo): valor menor que cero -> IllegalArgumentException
	@Test
	void stock_valorNegativo_lanzaExcepcion() {
		// Given
		BigDecimal valor = new BigDecimal("-0.5");
		String campo = "El stock actual";

		// When
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ValidacionDatos.stock(valor, campo));

		// Then
		assertEquals("El stock actual: no puede ser negativo", error.getMessage());
	}
}
