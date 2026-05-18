 Benchmark y visualización de estructuras de datos

1. Descripción general

Este proyecto corresponde al trabajo extraclase 2 del curso Algoritmos y Estructuras de Datos I (CE1103).

La aplicación permite ejecutar un benchmark para comparar el rendimiento de diferentes estructuras de datos implementadas en Java. El programa mide operaciones de inserción, búsqueda y borrado, mostrando los resultados en una interfaz gráfica con tabla comparativa, gráficos y opción de exportación a CSV.

Las estructuras comparadas son:

1. Arreglo
2. Lista enlazada simple
3. BST
4. AVL
5. Splay
6. Red-Black

Todas las estructuras reciben la misma secuencia de inserción y el mismo conjunto de búsquedas para que la comparación sea lo más justa posible.


2. JDK usado

Este proyecto fue desarrollado en Java. Se recomienda utilizar:

* JDK 17 o superior.
* JavaFX SDK 21, incluido en la carpeta `lib/javafx-sdk-21`.

Para verificar la versión instalada de Java:
java -version

Para verificar la versión del compilador:
javac -version

El proyecto incluye un archivo `run.bat` que permite compilar y ejecutar automáticamente la aplicación en Windows.


3. Interfaz usada

La interfaz gráfica fue desarrollada usando JavaFX.

JavaFX se utiliza para mostrar:

* Campos de entrada para configurar el benchmark.
* Selector de modo de búsqueda.
* Área para escribir o pegar búsquedas manuales.
* Casillas para seleccionar estructuras.
* Botón para ejecutar el benchmark.
* Tabla comparativa de resultados.
* Gráficos de tiempos, comparaciones y altura/tamaño.
* Botón para exportar resultados a CSV.

La interfaz permite que el usuario configure y ejecute las pruebas sin modificar directamente el código fuente.


4. Cómo compilar

La forma recomendada de compilar el proyecto en Windows es usando el archivo:
run.bat

Este archivo realiza los siguientes pasos:

1. Limpia o prepara la carpeta `out`.
2. Busca todos los archivos `.java` dentro de `src`.
3. Compila todas las clases del proyecto.
4. Usa JavaFX desde la ruta `lib\javafx-sdk-21\lib`.
5. Deja los archivos compilados dentro de la carpeta `out`.

El comando principal de compilación usado por el archivo `run.bat` es similar al siguiente:
javac --module-path "lib\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\gson-2.10.1.jar" -sourcepath "src" -d "out" @sources.txt

Donde:

* `--module-path` indica la ubicación de JavaFX.
* `--add-modules` indica los módulos de JavaFX usados.
* `-cp` agrega librerías externas necesarias.
* `-sourcepath` indica la carpeta del código fuente.
* `-d out` indica que los archivos compilados se guardan en `out`.
* `@sources.txt` contiene la lista de archivos `.java` a compilar.

También puede compilarse desde un IDE, siempre que se configure correctamente el JDK y JavaFX.


5. Cómo ejecutar

La forma recomendada de ejecutar el proyecto es abrir el archivo:
run.bat

Este archivo compila el proyecto y luego ejecuta la clase principal:
ui.BenchmarkApp

El comando de ejecución usado es similar al siguiente:
java --module-path "lib\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -cp "out;lib\gson-2.10.1.jar" ui.BenchmarkApp

También se puede ejecutar desde un IDE como:

* IntelliJ IDEA
* Eclipse
* NetBeans
* Visual Studio Code

En ese caso se debe configurar JavaFX y usar como clase principal:
ui.BenchmarkApp


6. Cómo usar la aplicación

Al abrir la aplicación, el usuario puede configurar los parámetros principales del benchmark.

Pasos básicos

1. Abrir la aplicación.
2. Ingresar el valor de `N`.
3. Ingresar la semilla.
4. Ingresar el valor de `W`.
5. Ingresar el valor de `R`.
6. Elegir el modo de búsqueda: automática o manual.
7. Si el modo es manual, escribir o pegar las claves a buscar.
8. Seleccionar las estructuras que se desean comparar.
9. Presionar el botón Ejecutar Benchmark.
10. Revisar la tabla comparativa.
11. Revisar los gráficos generados.
12. Exportar los resultados a CSV si se desea guardar la corrida.


7. Parámetros de entrada

N

`N` representa la cantidad de claves que se intentarán insertar en cada estructura.

Ejemplo:
N = 1000

Esto significa que se generarán 1000 claves para insertar en cada estructura seleccionada.


Semilla

La semilla es un número utilizado para generar la secuencia aleatoria de claves.

Ejemplo:
Semilla = 42

Si se usa la misma semilla y el mismo valor de `N`, el programa genera la misma secuencia de inserción.

Esto permite que los resultados sean reproducibles y que todas las estructuras sean evaluadas con los mismos datos.

Por ejemplo, si se ejecuta el benchmark con:
N = 1000
Semilla = 42

las estructuras reciben la misma secuencia de inserción generada a partir de esa semilla.


W - Warmup

`W` significa warmup o calentamiento.

Representa la cantidad de corridas completas que se ejecutan antes de medir los resultados finales.

Estas corridas no se toman en cuenta en la tabla final.

Su propósito es reducir efectos iniciales de la JVM, como:

* Carga de clases.
* Inicialización interna del programa.
* Optimización del compilador JIT.

Ejemplo:
W = 30

Esto significa que se ejecutarán 30 corridas de calentamiento antes de las corridas medidas.


R - Iteraciones medidas

`R` representa la cantidad de corridas que sí se miden y se promedian.

Ejemplo:
R = 10

Esto significa que el benchmark ejecutará 10 corridas medidas y calculará un promedio de los resultados.

El valor de `R` debe ser mayor o igual a 1.


8. Modos de búsqueda

La aplicación permite usar dos modos de búsqueda:

1. Búsqueda automática.
2. Búsqueda manual.

No se incluye carga de archivos para búsquedas. Las claves manuales se escriben o se pegan directamente en el área de texto de la interfaz.


Búsqueda automática

En este modo, el programa genera automáticamente un conjunto de claves de búsqueda.

La cantidad de búsquedas generadas es:
N / 2

Por ejemplo, si:
N = 1000

entonces el programa genera:
500 búsquedas

Estas búsquedas se generan usando una semilla relacionada con la semilla principal, por lo que el comportamiento es reproducible.

Las mismas búsquedas automáticas se aplican a todas las estructuras seleccionadas.


Búsqueda manual

En este modo, el usuario puede escribir o pegar directamente las claves que desea buscar.

Formato aceptado con comas:
10, 25, 80, 150, 300

También se acepta una clave por línea:
10
25
80
150
300

También se aceptan espacios o punto y coma como separadores.

Ejemplo con espacios:
10 25 80 150 300

Ejemplo con punto y coma:
10;25;80;150;300

El programa interpreta esos valores como números enteros y los usa como consultas de búsqueda para todas las estructuras seleccionadas.

Si el usuario elige el modo manual y deja el cuadro vacío, la aplicación muestra un error.


9. Flujo general del benchmark

El benchmark sigue este proceso:

1. Se leen los parámetros ingresados por el usuario.
2. Se genera la secuencia de inserción usando `N` y la semilla.
3. Se generan las búsquedas automáticas o se toman las búsquedas manuales ingresadas por el usuario.
4. Se ejecutan las corridas de calentamiento `W`.
5. Se ejecutan las corridas medidas `R`.
6. En cada corrida se insertan los mismos datos en cada estructura activa.
7. Luego se realizan las mismas búsquedas en cada estructura activa.
8. Después se ejecuta el borrado en las estructuras que lo permiten.
9. En Red-Black no se ejecuta borrado medido.
10. Se promedian los resultados.
11. Se muestran los resultados en la tabla.
12. Se generan los gráficos.
13. Se puede exportar el resultado a CSV.


10. Estructuras comparadas

El benchmark compara seis estructuras de datos:

1. Arreglo
2. Lista enlazada simple
3. BST
4. AVL
5. Splay
6. Red-Black

Todas las estructuras reciben la misma secuencia de inserción y el mismo conjunto de búsquedas para mantener una comparación consistente.


11. Arreglo

El arreglo es una estructura lineal donde los elementos se almacenan de forma contigua.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad     |
| Inserción | O(1) amortizado |
| Búsqueda  | O(n)            |
| Borrado   | O(n)            |


12. Lista enlazada simple

La lista enlazada simple está formada por nodos.

Cada nodo guarda un valor y una referencia al siguiente nodo.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad |
| Inserción | O(n)        |
| Búsqueda  | O(n)        |
| Borrado   | O(n)        |


13. BST

El BST, o árbol binario de búsqueda, organiza los datos usando la siguiente regla:

* Los valores menores van a la izquierda.
* Los valores mayores van a la derecha.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Promedio | Peor caso |
| Inserción | O(log n) | O(n)      |
| Búsqueda  | O(log n) | O(n)      |
| Borrado   | O(log n) | O(n)      |

El peor caso puede ocurrir si el árbol queda muy desbalanceado.


14. AVL

El AVL es un árbol binario de búsqueda auto-balanceado.

Después de insertar o borrar elementos, el árbol puede realizar rotaciones para mantener su altura controlada.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad |
| Inserción | O(log n)    |
| Búsqueda  | O(log n)    |
| Borrado   | O(log n)    |


15. Splay

El árbol Splay es un árbol binario de búsqueda autoajustable.

Cuando se accede a un nodo, este se mueve hacia la raíz mediante operaciones de reorganización llamadas splay.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad amortizada |
| Inserción | O(log n) amortizado    |
| Búsqueda  | O(log n) amortizado    |
| Borrado   | O(log n) amortizado    |


16. Red-Black

El árbol Red-Black es un árbol binario de búsqueda balanceado mediante colores.

Cada nodo puede ser rojo o negro, y el árbol debe cumplir reglas de balance para evitar que su altura crezca demasiado.

En este proyecto, Red-Black se usa únicamente para:

* Inserción
* Búsqueda

No se mide el borrado en Red-Black.

Por eso, en los resultados de borrado aparece:
N/A

Esto significa que la operación no aplica para esta estructura dentro del alcance del proyecto.

Complejidades esperadas:

| Operación | Complejidad |
| Inserción | O(log n)    |
| Búsqueda  | O(log n)    |
| Borrado   | N/A         |


17. Resultados mostrados

La aplicación muestra una tabla comparativa con los resultados de cada estructura.

La tabla incluye:

* Nombre de la estructura.
* Tiempo de inserción.
* Comparaciones de inserción.
* Complejidad teórica de inserción.
* Tiempo de búsqueda.
* Comparaciones de búsqueda.
* Complejidad teórica de búsqueda.
* Tiempo de borrado.
* Comparaciones de borrado.
* Complejidad teórica de borrado.
* Altura en estructuras de árbol.
* Tamaño de la estructura.

En Red-Black, las columnas relacionadas con borrado muestran `N/A`.


18. Exportación CSV

La aplicación permite exportar los resultados de la última corrida a un archivo CSV mediante el botón:
Exportar CSV

El botón se activa después de ejecutar el benchmark.

El CSV generado incluye:

* Nombre de la estructura.
* Tiempo de inserción.
* Tiempo de búsqueda.
* Tiempo de borrado.
* Comparaciones de inserción.
* Comparaciones de búsqueda.
* Comparaciones de borrado.
* Complejidad teórica de inserción.
* Complejidad teórica de búsqueda.
* Complejidad teórica de borrado.
* Altura.
* Tamaño.
* N.
* Semilla.
* W.
* R.
* Cantidad de búsquedas usadas.

Formato del CSV:
Estructura,InsertTime(ns),SearchTime(ns),DeleteTime(ns),InsertComp,SearchComp,DeleteComp,InsertO,SearchO,DeleteO,Height,Size,N,Seed,W,R,Queries
BST,456590,37170,63870,11492,6780,10668,"O(n) peor, O(log n) promedio","O(n) peor, O(log n) promedio","O(n) peor, O(log n) promedio",23,938,1000,42,30,10,500
AVL,341010,37070,87440,8592,4982,8424,O(log n),O(log n),O(log n),12,938,1000,42,30,10,500
Red-Black,284190,22850,N/A,8641,5008,N/A,O(log n),O(log n),N/A,12,938,1000,42,30,10,500

Los valores anteriores son solo un ejemplo. Los valores reales dependen de cada corrida.

En Red-Black, las columnas relacionadas con borrado muestran `N/A`, porque esa estructura no mide borrado dentro del alcance del proyecto.


19. Consideración sobre claves repetidas

El valor `N` representa la cantidad de intentos de inserción generados para cada corrida.

Las claves se generan aleatoriamente usando la semilla. Debido a esto, pueden aparecer claves repetidas.

En las estructuras de árbol, las claves repetidas no se insertan como nodos nuevos. Por esta razón, el tamaño final de los árboles puede ser menor que `N`.

Por ejemplo, si:
N = 1000


puede ocurrir que solo existan 938 claves únicas. En ese caso, los árboles mostrarán tamaño 938.

En cambio, estructuras lineales como el arreglo y la lista simple pueden almacenar valores repetidos, por lo que pueden mostrar tamaño 1000.


20. Consideraciones sobre la medición

Los resultados pueden variar entre ejecuciones debido a factores como:

* Procesador del equipo.
* Memoria disponible.
* Carga del sistema operativo.
* Optimización interna de la JVM.
* Recolección de basura de Java.
* Cantidad de datos evaluados.
* Valor de la semilla.
* Cantidad de iteraciones medidas.

Por eso se usan los valores `W` y `R`, para hacer la medición más estable.