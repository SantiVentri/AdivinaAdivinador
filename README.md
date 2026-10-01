# AdivinaAdivinador v2.0

Juego de consola inspirado en **"¿Quién es quién?"** con temática de Harry Potter. Cada jugador tiene un personaje secreto y, por turnos, hace preguntas sobre sus características para ir descartando candidatos del tablero hasta arriesgar quién es el personaje del rival.

## Características

- **Modo Jugador vs. Máquinas**: desafío de dos rondas. Primero jugás contra la _Máquina Aleatoria_; si ganás, se suma la _Máquina Asertiva_, que además hereda las preguntas que ya había hecho la aleatoria.
- **Modo Máquina vs. Máquina**: mirás como espectador una partida entre la _Máquina Asertiva_ y la _Máquina Aleatoria_, avanzando turno a turno con un botón de Continuar.
- **Marcador de records** persistido en `scores.txt` (encabezado y una línea por jugador,
  formato `nombre;jugadas;rondas;partidas`). Se puede ver desde la pantalla de Puntajes.
- **23 personajes** del universo de Harry Potter con 8 características filtrables.

## Cómo ejecutar desde el IDE

1. Abrir el proyecto en InteliJ
2. Ejecutar el archivo main.Main

## Cómo se juega

1. Ingresás tu nombre (entre 3 y 11 letras, sin números ni espacios).
2. Elegís una opción del menú principal: jugar, ver puntajes o salir.
3. Al entrar a un modo, elegís tu personaje secreto de la lista.
4. En cada turno podés:
   - **Hacer una pregunta**: elegís una característica y un valor; el rival
     responde Sí/No y tu tablero se filtra automáticamente.
   - **Arriesgar un personaje**: si acertás, ganás la partida; si fallás ese personaje se descarta y el juego continúa.
5. En el modo Jugador vs. Máquinas, cada desafío que empezás suma una **partida jugada** (aunque lo abandones), cada ronda que superás suma una **ronda ganada** al marcador; si superás las dos rondas, además sumás una **partida ganada**.

### Características filtrables (`TipoFiltro`)

| Filtro          | Valores posibles                                      |
| --------------- | ----------------------------------------------------- |
| `GENERO`        | MASCULINO, FEMENINO                                   |
| `EDAD`          | ADOLESCENTE, ADULTO, ANCIANO                          |
| `COLOR_PELO`    | COLORADO, NEGRO, GRIS, MARRON, ROSA, AMARILLO, BLANCO |
| `CALVICIE`      | true / false                                          |
| `LENTES`        | true / false                                          |
| `CASA_HOGWARTS` | GRYFFINDOR, SLYTHERIN, HUFFLEPUFF, RAVENCLAW          |
| `ALUMNO`        | true / false                                          |
| `SANGRE_LIMPIA` | MAGO, MESTIZO, MUGGLE                                 |

## Estructura del proyecto

```
src/
├── main/
│   └── Main.java                     # Punto de entrada, abre la VentanaPrincipal
├── game/
│   ├── MotorJuego.java               # Alterna turnos, resuelve preguntas e intentos y declara al ganador
│   ├── ModoJuego.java                # Clase vacía, todavía sin uso
│   ├── ModoJugadorVsMaquinas.java    # Desafío de 2 rondas contra las máquinas
│   └── ModoMaquinaVsMaquina.java     # Partida Asertiva vs. Aleatoria, avanzada de a un turno (espectador)
├── model/
│   ├── Personaje.java                # Personaje y su lógica de cumpleFiltro(...)
│   ├── Tablero.java                  # Personajes restantes: filtrado, descarte y búsqueda binaria por id
│   ├── Filtro.java                   # Característica preguntable: tipo, nombre y valores
│   ├── FiltroAplicado.java           # Par (tipo, valor) de una pregunta
│   ├── TipoFiltro.java               # Enum de características filtrables
│   └── Genero, Edad, ColorPelo,
│       CasaHogwarts, SangreLimpia    # Enums de dominio
├── players/
│   ├── Jugador.java                  # Clase abstracta base (nombre, personaje secreto, tablero); responde preguntas
│   ├── JugadorHumano.java            # Jugador cuyas jugadas llegan desde la interfaz
│   ├── JugadorMaquina.java           # Clase abstracta base de las máquinas (estrategia propia)
│   ├── MaquinaAleatoria.java         # Pregunta al azar y arriesga con probabilidad 0.3
│   ├── MaquinaAsertiva.java          # Elige el filtro que mejor divide el tablero
│   ├── HistorialConsultas.java       # Historial compartido de preguntas y respuestas, filtrable por jugador
│   └── Consulta.java                 # Una entrada del historial (jugador, pregunta y respuesta)
├── score/
│   ├── RepositorioPuntajes.java      # Interfaz del marcador de records
│   ├── Puntaje.java                  # Fila del marcador: jugador, partidas jugadas, rondas y partidas ganadas
│   └── ScoreRepository.java          # Implementación: carga/guarda el marcador en scores.txt
├── ui/                               # Interfaz Swing (los .form son del GUI Designer de IntelliJ)
│   ├── VentanaPrincipal.java         # Ventana principal: crea las pantallas y navega entre ellas (CardLayout)
│   ├── PanelBienvenida               # Ingreso y validación del nombre del jugador
│   ├── PanelMenuPrincipal            # Menú: jugar, puntajes o salir
│   ├── PanelModosDeJuego             # Elección del modo de juego o volver al menú
│   ├── PanelEleccionPersonaje        # Elección del personaje secreto
│   ├── PanelJuego                    # Pantalla del modo Jugador vs. Máquinas: tablero, filtros, logs y arriesgar
│   ├── PanelEspectador               # Pantalla del modo Máquina vs. Máquina
│   ├── PanelFiltros                  # Botón por característica y luego por valor para armar la pregunta
│   ├── PanelLogs                     # Panel lateral con los mensajes de la partida
│   ├── PanelPuntajes                 # Tabla del marcador de records
│   ├── PanelTablero.java             # Grilla 4x6 de tarjetas de personajes
│   └── TarjetaPersonaje.java         # Botón de un personaje, con estilo seleccionada/descartada
└── utils/
    ├── PersonajeFactory.java         # Crea los 23 personajes, los ordena por género (MergeSort) y les asigna id autoincremental
    ├── MergeSort.java                # Ordenamiento genérico Merge Sort (divide y conquista, estable, O(n log n))
    ├── FiltroFactory.java            # Crea los 8 filtros con sus valores posibles
    └── Registro.java                 # Acumula los mensajes de la partida hasta que la pantalla los muestra
```

## Diseño y comportamiento de las máquinas

- **MaquinaAleatoria**: elige tipo y valor de pregunta al azar (evitando repetir), y arriesga con probabilidad `0.3` por turno (o forzado cuando queda un solo candidato).
- **MaquinaAsertiva**: para cada filtro no preguntado calcula cuántos personajes cumplirían y elige el que deja una división más cercana a la mitad (máxima información). Solo arriesga cuando queda un candidato o no le quedan preguntas nuevas.
- **Historial compartido**: en el modo Jugador vs. Máquinas, la Máquina Asertiva arranca replicando las preguntas y respuestas que ya había hecho la Máquina Aleatoria sobre el personaje secreto del jugador.

## Persistencia

El archivo `scores.txt` se genera automáticamente en la raíz del proyecto. Tiene una fila de encabezado y una línea por jugador:

```
nombre;jugadas;rondas;partidas
Juan;7;5;2
```

En el modo Jugador vs. Máquinas, al empezar cada desafío se llama a `ScoreRepository.registrarPartidaJugada(nombre)`,
cada ronda ganada llama a `ScoreRepository.registrarRondaGanada(nombre)` y, al ganar la segunda ronda, también a
`registrarPartidaGanada(nombre)`.
La pantalla de Puntajes ordena por partidas ganadas y, a igualdad, por rondas ganadas, usando `MergeSort`. Al ser un
ordenamiento estable, si dos jugadores empatan en ambos criterios se mantiene el orden en que se registraron. El archivo
se guarda en orden de registro: el ranking se calcula al consultarlo.
