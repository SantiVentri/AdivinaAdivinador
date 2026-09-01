# Informe de robustez — AdivinaAdivinador

> Batería de tests + simulaciones masivas para intentar romper el juego.
> Fecha: 2026-09-01 · Rama: `test/robustez` (sale de `feature/gestor-y-modos`) · JDK 25
> **Modo "reportar primero": NADA de esto está arreglado todavía.** Este informe lista lo que se
> encontró para que decidas qué tocar.

---

## 1. Resumen

Se agregó un harness en Java puro (sin dependencias) bajo `test/`, que corre con:

```
bash  test/run-tests.sh
# o
powershell -ExecutionPolicy Bypass -File test\run-tests.ps1
```

Qué hace (146 aserciones + fuzzing + simulación):

| Bloque | Qué prueba |
|---|---|
| Unitarios | `Personaje`, `Tablero`, `FiltroAplicado`, `HistorialConsultas`, `PersonajeFactory`, `ScoreRepository` |
| `JugadorHumano` | menús con `Scanner` sobre guiones de texto, captura de `stdout` |
| `MotorJuego` | con jugadores "stub" programables: ganador, arriesgue fallido, alternancia, hook, empate, bucle |
| Máquinas | heurística de la asertiva, no-repetición, cuándo arriesga; "drive" en solitario de cada máquina contra los 23 personajes |
| Fuzz de entrada | 6500 guiones de basura aleatoria (unicode, enteros gigantes, control chars, `%s`, `'; DROP TABLE`, etc.) contra la capa interactiva |
| Simulación | **4000 partidas** `MotorJuego` (Asertiva vs Aleatoria) + **120** corridas reales de `ModoMaquinaVsMaquina` + **23** playthroughs completos de `ModoJugadorVsMaquinas`, todos con watchdog anti-cuelgue |
| IO / EOF | qué pasa cuando se agota/cierra el `stdin` |
| Smoke | `Main.main` de punta a punta navegando menús |

### Veredicto

- **No se encontró ninguna forma de hacer crashear el juego con entradas de teclado.** 6500 guiones de basura: cero excepciones inesperadas. Todos los bucles de menú reintentan bien.
- **No se encontró ningún cuelgue ni excepción** en 4000 partidas de máquina vs máquina, 120 corridas del modo espectador y 23 partidas completas de jugador vs máquinas.
- La estrategia de la Máquina Asertiva **funciona**: le gana a la Aleatoria ~68% de las veces. La partida más larga observada tuvo 47 preguntas.
- **La única forma de "romperlo" es cerrándole la entrada** (pipe, redirección de archivo, `Ctrl+Z`/`Ctrl+D`, terminal cerrada): ahí sale `NoSuchElementException` con stack trace en vez de terminar prolijo. Afecta a los 3 modos y al menú principal.
- Hay **1 riesgo estructural ALTA (latente)**: `MotorJuego` no tiene tope de turnos; hoy las 3 clases de `Jugador` que existen no lo disparan, pero el motor no está blindado.

### Conteo de hallazgos

| Severidad | Cantidad | IDs |
|---|---|---|
| 🔴 ALTA (latente) | 1 | MOTOR-LOOP-1 |
| 🟠 MEDIA | 9 | IO-EOF-0/1/2/3, HUM-EOF-1, FACT-TWINS-1, SCORE-SEP-1, SCORE-PARSE-1, ENC-1 |
| 🟡 BAJA | 13 | PERS-BOOL-1, PERS-ORD-1, TAB-EQ-1, TAB-NULL-1, FILT-CLAVE-1, HIST-NULL-1, HIST-SHARE-1, SCORE-PATH-1, SCORE-DOUBLE-1, MOTOR-EMPATE-1, MOTOR-EQ-1, MAIN-MSG-1, MAIN-STATIC-1 |

Ninguno impide compilar (`javac -Xlint:all` limpio) ni jugar una partida normal.

---

## 2. Hallazgos

### 🔴 ALTA

#### MOTOR-LOOP-1 — `MotorJuego` no tiene tope de turnos
- **Qué pasa:** en `MotorJuego.jugarTurno`, si el jugador no arriesga (`arriesgarPersonaje()==null`) y no le quedan preguntas nuevas (`hacerPregunta()==null`), imprime "pasa el turno" y vuelve. El `while (!partidaTerminada)` de `jugar()` solo corta si **los dos** tableros quedan vacíos. Con ambos jugadores pasando indefinidamente → bucle infinito imprimiendo turnos.
- **Reproducción:** test `MotorJuego - BUCLE INFINITO...`: dos `StubJugador` que siempre devuelven `(null, null)`. El motor pasó de 5000 turnos sin terminar (tope artificial del stub).
- **Alcance real hoy:** **no se dispara** con las 3 implementaciones actuales de `Jugador`: `MaquinaAleatoria.hacerPregunta` nunca devuelve `null` (tras 50 intentos repite una pregunta), `MaquinaAsertiva.arriesgarPersonaje` siempre arriesga cuando se queda sin preguntas, y `JugadorHumano` siempre termina eligiendo algo. Confirmado por 4000 partidas sin cuelgues.
- **Por qué igual es ALTA:** el motor no tiene ninguna red de seguridad. Cualquier `Jugador` nuevo (otra máquina, un bugfix) que devuelva `(null, null)` aunque sea transitoriamente lo cuelga, e imprime sin parar.
- **Sugerencia (no aplicada):** tope de turnos duro (p. ej. `personajes.size() * 4`) que corte en empate técnico, o cortar si el tablero del activo no cambió en N turnos y no arriesgó.

---

### 🟠 MEDIA

#### IO-EOF-0 / IO-EOF-1 / IO-EOF-2 / IO-EOF-3 — fin de entrada no controlado
- **Qué pasa:** ningún `scanner.nextLine()` está protegido contra el fin de entrada. Apenas se agota o se cierra el `stdin`, sale `java.util.NoSuchElementException: No line found` con stack trace.
  - **IO-EOF-1:** `ModoMaquinaVsMaquina` — el hook "Presioná Enter para continuar" (`Consola.esperarEnter`) revienta al primer turno si no hay entrada.
  - **IO-EOF-2:** `ModoJugadorVsMaquinas.elegirPersonajeSecreto` — solo atrapa `NumberFormatException`; el `NoSuchElementException` pasa de largo.
  - **IO-EOF-3:** con el personaje ya elegido, el primer turno del humano (`JugadorHumano.leerOpcionEnRango`) corta igual.
  - **IO-EOF-0 / HUM-EOF-1:** lo mismo en `Main.pedirNombre`, `Main.leerOpcion`, `JugadorHumano.arriesgarPersonaje`.
- **Reproducción:** tests `IoRobustezTest` y `JugadorHumanoTest` con `stdin` vacío o cortado a mitad. También se vio en pruebas manuales piped.
- **Impacto:** cualquier ejecución no interactiva (correr con `< input.txt`, pipe, cerrar la terminal, `Ctrl+Z`) termina con un volcado de excepción feo en vez de "Saliendo... ¡Gracias por jugar!".
- **Sugerencia (no aplicada):** un helper `leerLinea(Scanner)` que haga `if (!sc.hasNextLine()) { /* salir prolijo */ }` antes de cada `nextLine()`, o un `try/catch (NoSuchElementException)` en el `main` que corte limpio.

#### FACT-TWINS-1 — Fred y George Weasley son indistinguibles
- **Qué pasa:** en `PersonajeFactory`, Fred Weasley y George Weasley tienen los **8** atributos idénticos (masculino, adolescente, pelo colorado, no calvo, sin lentes, Gryffindor, mago, alumno). Ninguna combinación de preguntas los separa.
- **Reproducción:** test `PersonajeFactory - hay dos personajes indistinguibles` (comparación atributo por atributo). También se ve en la simulación: la partida más larga siempre involucra a Fred/George como secreto.
- **Impacto:** si el personaje secreto es uno de los dos, el rival (máquina o humano) queda siempre con 2 candidatos y tiene que adivinar a ciegas (50%). No rompe el juego, pero contradice el "características distinguibles" de la consigna.
- **Sugerencia (no aplicada):** cambiarle un atributo a uno de los dos (p. ej. George con lentes), o aceptarlo y documentarlo como decisión.

#### SCORE-SEP-1 — un nombre con `;` corrompe `scores.txt`
- **Qué pasa:** `ScoreRepository.guardar` escribe `nombre;victorias`. Si el nombre tiene `;` (ej. `ro;be`), la línea queda `ro;be;1`. Al releer, `linea.split(";", 2)` da `["ro", "be;1"]` y `Integer.parseInt("be;1")` tira `NumberFormatException`.
- **Reproducción:** test `ScoreRepository - nombre con ';' corrompe el archivo`. Releído: `Ana=1, Zoe=0` (se esperaba `1` y `1`).
- **Impacto doble:**
  1. `Main.pedirNombre` solo valida el **largo** (3..12), no los caracteres → el usuario puede meter `;` sin problema.
  2. El `catch` de `cargar()` envuelve **todo el `while`**, así que una sola línea rota **descarta todos los puntajes que vienen después** en el archivo.
- **Sugerencia (no aplicada):** separador más seguro (tab, o escapar), o `split` desde la derecha, o validar el nombre; y mover el `try/catch` adentro del `while` para saltear solo la línea mala.

#### SCORE-PARSE-1 — una línea inválida en `scores.txt` tira abajo toda la carga
- **Qué pasa:** mismo mecanismo que SCORE-SEP-1 pero por archivo editado a mano / corrupto. Con:
  ```
  basura sin separador
  Pepe;3
  Otro;noEsNumero
  Depp;5
  ```
  se carga `Pepe=3` y **se pierde `Depp;5`** porque `Otro;noEsNumero` aborta el bucle.
- **Reproducción:** test `ScoreRepository - archivo con líneas basura`.
- **Sugerencia (no aplicada):** `try/catch` por línea; log de la línea salteada y seguir.

#### ENC-1 — acentos ilegibles en consolas Windows con code page legacy *(inspección de código)*
- **Qué pasa:** el juego imprime mucho texto con acentos y `¿ ¡`. En consolas Windows con code page cp850/cp1252 (y al pipear) sale `M�quina`, `Presion�`, etc. `System.out` no fuerza UTF-8.
- **Evidencia:** salida observada al correr el juego y el harness por pipe.
- **Sugerencia (no aplicada):** envolver `System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8))` al arrancar `Main`, o documentar `-Dstdout.encoding=UTF-8`.

---

### 🟡 BAJA

| ID | Dónde | Qué pasa |
|---|---|---|
| **PERS-BOOL-1** | `Personaje.cumpleFiltro` | Para `CALVICIE`/`LENTES`/`ALUMNO` usa `Boolean.parseBoolean`: cualquier string que no sea `"true"` (ci) cuenta como `false` **sin avisar**. Un valor mal tipeado no da error, solo una respuesta incorrecta. |
| **PERS-ORD-1** | `Personaje.asignarOrden` | Acepta `0` y negativos (no valida rango). Con `-1` el personaje vuelve a considerarse "sin asignar" (el guardián usa `-1` de centinela) y se lo podría re-numerar. |
| **TAB-EQ-1 / MOTOR-EQ-1** | `Tablero.sacarPersonaje`, `Jugador.esPersonajeSecreto` | Dependen de **identidad de instancia** (`Personaje` no implementa `equals`/`hashCode`). Hoy anda porque toda la partida comparte la lista de `PersonajeFactory`; si algo reconstruye personajes, "arriesgar por valor" deja de funcionar y `sacarPersonaje` no saca nada. |
| **TAB-NULL-1** | `Tablero.buscarPorNombre(null)` | Tira `NullPointerException` (`null.trim()`). No se llega con `null` desde el juego, pero no está blindado. |
| **FILT-CLAVE-1** | `FiltroAplicado.clave` | No normaliza el valor: el humano genera `COLOR_PELO=negro` y las máquinas `COLOR_PELO=NEGRO`. El historial las trata igual (compara con `equalsIgnoreCase`), pero cualquier comparación directa de claves por `String.equals` fallaría. |
| **HIST-NULL-1** | `HistorialConsultas.yaFuePreguntado(nombre, null)` | `NullPointerException` (`null.trim()`). Latente. |
| **HIST-SHARE-1** | `HistorialConsultas` en Máquina vs Máquina | Las dos máquinas comparten el objeto pero cada una consulta solo por su propio nombre: la "ventaja" de la máquina 2 de conocer las preguntas de la máquina 1 **no se usa** en el modo espectador. (Decisión de diseño ya charlada, no crash.) |
| **SCORE-PATH-1** | `ScoreRepository` | Ruta `"scores.txt"` fija y relativa al directorio de ejecución: no es testeable en aislado y el marcador "se mueve" según desde dónde se lance el juego (doble click vs consola vs IDE). |
| **SCORE-DOUBLE-1** | `ModoJugadorVsMaquinas` | Llama `registrarVictoria(nombre)` **dos veces** al completar el desafío (una por la Aleatoria, otra por la Asertiva): un desafío completo suma **2** al marcador. Ambigüedad de la consigna ("partidas ganadas"): ¿1 por desafío? ¿1 por ronda (y entonces perder la ronda 1 también debería contar como partida jugada)? |
| **MOTOR-EMPATE-1** | `MotorJuego` | En el empate (ambos tableros vacíos) hace `break` sin setear `partidaTerminada`. `jugar()` devuelve `null` pero `isPartidaTerminada()` sigue en `false`: un llamador que se guíe por ese getter cree que la partida sigue. |
| **MAIN-MSG-1** | `Main.pedirNombre` | El chequeo del largo máximo es `> 12` (12 letras se aceptan) pero el mensaje dice "menos de 12 letras". Inconsistencia de texto. |
| **MAIN-STATIC-1** | `Main` | El `Scanner` `static` atado a `System.in` impide testear `Main` más de una vez por proceso y obliga a fijar `System.in` antes de cargar la clase. Recibir el `Scanner` por parámetro lo haría testeable de punta a punta. |

### Observaciones menores (sin ID)
- `PersonajeFactory.crearPersonajes()` devuelve la lista interna **mutable** (no `unmodifiableList`), y `disponer(...)` ordena in-place el argumento recibido. Hoy nadie la muta porque `Tablero` la copia, pero es un contrato flojo.
- `MotorJuego.resolverIntento` arma un mensaje con formato pobre: `"Incorrecto. " + activo.getNombre() + " El juego continua!!."` (queda `Incorrecto. Máquina Aleatoria El juego continua!!.`).
- `MotorJuego` imprime la pregunta del humano en minúscula y la de las máquinas en mayúscula (por FILT-CLAVE-1). Cosmético.

---

## 3. Qué se intentó romper y **aguantó** (resultados positivos)

| Escenario | Resultado |
|---|---|
| 4000 guiones de basura aleatoria a `JugadorHumano.arriesgarPersonaje` | 0 excepciones inesperadas; todos resueltos |
| 2000 guiones de basura a `JugadorHumano.hacerPregunta` | siempre arma un `FiltroAplicado` válido |
| 500 guiones con enteros extremos (`2147483648`, negativos, `99999…`) en la selección de personaje | 0 excepciones raras |
| 4000 partidas `MotorJuego` Asertiva vs Aleatoria (secretos y orden al azar) | 0 excepciones, 0 cuelgues, 0 empates; asertiva 68% |
| 120 corridas reales de `ModoMaquinaVsMaquina` con entrada infinita | 0 excepciones, 0 cuelgues |
| 23 partidas completas de `ModoJugadorVsMaquinas` (incl. 4 que llegaron a ronda 2) | 0 excepciones, 0 cuelgues; `replicarPreguntasPrevias` ejercitado |
| "Drive" en solitario de cada máquina contra los 23 personajes | siempre termina y acierta; peor caso 27 pasos |
| `MaquinaAsertiva` — filtro elegido vs. mejor filtro posible (23 personajes) | siempre elige la partición óptima (más cercana a la mitad) |
| `MaquinaAsertiva` / `MaquinaAleatoria` — repetición de preguntas | la asertiva nunca repite; la aleatoria nunca devuelve `null` |
| `Tablero` filtrado (respuestas sí/no), `reiniciar`, `sacarPersonaje`, listas inmutables | correcto |
| `Personaje.asignarOrden` una sola vez; `elegirPersonaje` una sola vez | lanzan `IllegalStateException` en el segundo intento |
| `MotorJuego` validaciones de constructor (nulls, jugador sin secreto) | lanzan la excepción correcta |
| `Main.main` navegando menús (nombre corto, nombre largo, ver puntajes, salir) | salida limpia |

---

## 4. Cómo correr los tests

```bash
# desde la raíz del repo
bash test/run-tests.sh
```
o en PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File test\run-tests.ps1
```

El script compila `src` + `test` a `bin-test/` (ignorado por git) y corre `tests.TestMain`
**desde un directorio temporal** (porque `ScoreRepository` escribe `scores.txt` en el CWD).
Necesita `javac`/`java` en el `PATH` (probado con JDK 25); si no, exportá `JAVAC` y `JAVA`
apuntando al binario.

Exit code `0` si no hay FAILs. Los "findings" catalogados no cambian el exit code:
son los defectos ya confirmados que están en este informe.
