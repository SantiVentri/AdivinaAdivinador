# AdivinaAdivinador v1.0

Juego de consola inspirado en **"¿Quién es quién?"** con temática de Harry Potter. Cada jugador tiene un personaje secreto y, por turnos, hace preguntas sobre sus características para ir descartando candidatos del tablero hasta arriesgar quién es el personaje del rival.

## Características

- **Modo Jugador vs. Máquinas**: desafío de dos rondas. Primero jugás contra la *Máquina Aleatoria*; si ganás, se suma la *Máquina Asertiva*, que además hereda las preguntas que ya había hecho la aleatoria.
- **Modo Máquina vs. Máquina**: mirás como espectador una partida entre la *Máquina Asertiva* y la *Máquina Aleatoria*, avanzando turno a turno con Enter.
- **Marcador de records** persistido en `scores.txt` (una línea por jugador,
  formato `nombre;victorias`).
- **23 personajes** del universo de Harry Potter con 8 características filtrables.

## Cómo ejecutar
1. Abrir un editor como Eclipse
2. Importar como proyecto
3. Ejecutar main.Main

## Cómo se juega

1. Ingresás tu nombre (entre 3 y 12 caracteres).
2. Elegís una opción del menú principal: jugar, ver puntajes o salir.
3. Al entrar a un modo, elegís tu personaje secreto de la lista.
4. En cada turno podés:
   - **Hacer una pregunta**: elegís una característica y un valor; el rival
   responde Sí/No y tu tablero se filtra automáticamente.
   - **Arriesgar un personaje**: si acertás, ganás la partida; si fallás ese personaje se descarta y el juego continúa.
5. Ganás una victoria (registrada en el marcador) por cada ronda que superás en el modo Jugador vs. Máquinas.

### Características filtrables (`TipoFiltro`)

| Filtro          | Valores posibles                                              |
|-----------------|--------------------------------------------------------------|
| `GENERO`        | MASCULINO, FEMENINO                                          |
| `EDAD`          | ADOLESCENTE, ADULTO, ANCIANO                                 |
| `COLOR_PELO`    | COLORADO, NEGRO, GRIS, MARRON, ROSA, AMARILLO, BLANCO       |
| `CALVICIE`      | true / false                                                |
| `LENTES`        | true / false                                                |
| `CASA_HOGWARTS` | GRYFFINDOR, SLYTHERIN, HUFFLEPUFF, RAVENCLAW                |
| `ALUMNO`        | true / false                                                |
| `SANGRE_LIMPIA` | MAGO, MESTIZO, MUGGLE                                        |

## Estructura del proyecto

```
src/
├── main/
│   └── Main.java                   # Punto de entrada, menús de consola
├── game/
│   ├── MotorJuego.java             # Bucle de turnos, resolución de intentos y ganador
│   ├── ModoJugadorVsMaquinas.java  # Desafío de 2 rondas contra las máquinas
│   └── ModoMaquinaVsMaquina.java   # Partida entre máquinas (modo espectador)
├── model/
│   ├── Personaje.java              # Personaje y su lógica de cumpleFiltro(...)
│   ├── Tablero.java                # Lista de personajes restantes y filtrado
│   ├── FiltroAplicado.java         # Par (tipo, valor) de una pregunta
│   ├── TipoFiltro.java             # Enum de características filtrables
│   ├── Genero, Edad, ColorPelo,
│   │   CasaHogwarts, SangreLimpia  # Enums de dominio
├── players/
│   ├── Jugador.java                # Clase abstracta base (personaje secreto, tablero)
│   ├── JugadorHumano.java          # Interacción por consola
│   ├── MaquinaAleatoria.java       # Pregunta y arriesga al azar
│   ├── MaquinaAsertiva.java        # Elige el filtro que mejor divide el tablero
│   ├── HistorialConsultas.java     # Registro de preguntas por jugador
│   └── Consulta.java               # Una entrada del historial
├── score/
│   └── ScoreRepository.java        # Carga/guarda el marcador en scores.txt
└── utils/
    ├── PersonajeFactory.java       # Crea y ordena los 22 personajes
    └── Consola.java                # Utilidad "presioná Enter para continuar"
```

## Diseño y comportamiento de las máquinas

- **MaquinaAleatoria**: elige tipo y valor de pregunta al azar (evitando repetir), y arriesga con probabilidad `0.3` por turno (o forzado cuando queda un solo candidato).
- **MaquinaAsertiva**: para cada filtro no preguntado calcula cuántos personajes cumplirían y elige el que deja una división más cercana a la mitad (máxima información). Solo arriesga cuando queda un candidato o no le quedan preguntas nuevas.
- **Historial compartido**: en el modo Jugador vs. Máquinas, la Máquina Asertiva arranca replicando las preguntas y respuestas que ya había hecho la Máquina Aleatoria sobre el personaje secreto del jugador.

## Persistencia

El archivo `scores.txt` se genera en runtime en la raíz del proyecto y está
ignorado por Git (`.gitignore`). Cada victoria en el modo Jugador vs. Máquinas llama a `ScoreRepository.registrarVictoria(nombre)`, que reescribe el archivo completo.