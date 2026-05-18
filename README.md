# Benchmark y visualización de estructuras de datos

## 1. JDK usado

Este proyecto fue desarrollado en Java y se recomienda ejecutarlo con:
JDK 17 o superior

Se recomienda usar JDK 17 porque es una versión estable y compatible con JavaFX.

Para verificar la versión instalada:
java -version

Para verificar el compilador:
javac -version


---

## 2. Interfaz usada

La interfaz gráfica del proyecto fue desarrollada usando JavaFX.

JavaFX se utiliza para mostrar:

* Campos de entrada para configurar el benchmark.
* Casillas para seleccionar estructuras.
* Botón para ejecutar las pruebas.
* Tabla comparativa de resultados.
* Gráficos de tiempos, comparaciones y altura/tamaño.
* Visualización de los resultados obtenidos por cada estructura.

La interfaz permite que el usuario ejecute pruebas sin modificar directamente el código fuente.

---

## 3. Cómo compilar

Para compilar el proyecto se necesita tener instalado:

* JDK 17 o superior.
* JavaFX SDK.

Si se compila desde consola en Windows, se puede usar un comando como el siguiente:
javac --module-path "RUTA_JAVAFX\lib" --add-modules javafx.controls,javafx.fxml -d out src/*.java


Ejemplo:
javac --module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml -d out src/*.java


Donde:

* `--module-path` indica la ruta donde está JavaFX.
* `--add-modules` indica los módulos de JavaFX que se usarán.
* `-d out` indica que los archivos compilados se guardarán en la carpeta `out`.
* `src/*.java` indica que se compilarán los archivos `.java` dentro de la carpeta `src`.

Si el proyecto tiene subcarpetas dentro de `src`, se puede usar:
javac --module-path "RUTA_JAVAFX\lib" --add-modules javafx.controls,javafx.fxml -d out src//*.java


---

## 4. Cómo ejecutar

Después de compilar, el proyecto se puede ejecutar con:
java --module-path "RUTA_JAVAFX\lib" --add-modules javafx.controls,javafx.fxml -cp out Main


Ejemplo:
java --module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml -cp out Main


Importante:
Si la clase principal del proyecto no se llama `Main`, se debe cambiar `Main` por el nombre real de la clase principal.

Por ejemplo:
java --module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml -cp out BenchmarkApp


También puede ejecutarse desde un IDE como:

* IntelliJ IDEA
* Eclipse
* NetBeans
* Visual Studio Code

En ese caso se debe configurar JavaFX como librería externa y agregar los módulos necesarios en la configuración de ejecución.

---

## 5. Cómo usar la aplicación

Al abrir la aplicación, el usuario puede configurar los parámetros principales del benchmark.

### Pasos básicos

1. Abrir la aplicación.
2. Ingresar el valor de `N`.
3. Ingresar la semilla.
4. Ingresar el valor de `W`.
5. Ingresar el valor de `R`.
6. Seleccionar las estructuras que se desean comparar.
7. Elegir el modo de búsqueda.
8. Ejecutar el benchmark.
9. Revisar la tabla comparativa.
10. Revisar los gráficos generados.
11. Exportar los resultados a CSV si se necesita guardar la corrida.

---

## 6. Parámetros de entrada

### N

Representa la cantidad de claves que se insertarán en cada estructura.

Ejemplo:
N = 1000


Esto significa que se generarán 1000 claves aleatorias para insertar en cada estructura seleccionada.

---

### Semilla

La semilla es un número utilizado para generar la secuencia aleatoria de claves.

Ejemplo:
Semilla = 42


Si se usa la misma semilla y el mismo valor de `N`, el programa genera la misma secuencia de inserción.

Esto permite que los resultados sean reproducibles y que todas las estructuras sean evaluadas con los mismos datos.

Por ejemplo, si se ejecuta el benchmark con:
N = 1000
Semilla = 42


todas las estructuras reciben exactamente las mismas 1000 claves en el mismo orden.

---

### W - Warmup

`W` significa warmup o calentamiento.

Representa la cantidad de corridas completas que se ejecutan antes de medir los resultados finales.

Estas corridas no se toman en cuenta en la tabla final.

Su propósito es reducir efectos iniciales de la JVM, como:

* Carga de clases.
* Optimización del compilador JIT.
* Inicialización interna del programa.

Ejemplo:
W = 1


Esto significa que se ejecutará una corrida de calentamiento antes de las corridas medidas.

---

### R - Iteraciones medidas

`R` representa la cantidad de corridas que sí se miden y se promedian.

Ejemplo:
R = 3


Esto significa que el benchmark ejecutará 3 corridas medidas y calculará un promedio de los resultados.

El valor de `R` debe ser mayor o igual a 1.

---

## 7. Formato de búsquedas manuales o por archivo

La aplicación permite trabajar con búsquedas generadas automáticamente y también puede aceptar búsquedas manuales o desde archivo, según el modo seleccionado.

### Búsqueda automática

En este modo, el programa genera automáticamente un conjunto de claves de búsqueda usando una regla interna documentada.

Estas búsquedas se aplican por igual a todas las estructuras seleccionadas.

Ejemplo:
Cantidad de búsquedas = N / 2


Si `N = 1000`, entonces se generan 500 búsquedas.

---

### Búsqueda manual

En el modo manual, el usuario puede escribir directamente las claves que desea buscar.

Formato aceptado con comas:
10, 25, 80, 150, 300


También puede escribirse una clave por línea:
10
25
80
150
300


El programa interpreta esos valores como claves enteras que serán buscadas en cada estructura activa.

---

### Búsqueda por archivo

En el modo por archivo, el usuario puede cargar un archivo `.txt` o `.csv` con las claves de búsqueda.

Formato válido en archivo `.txt`:
10
25
80
150
300


Formato válido en archivo `.csv`:
10,25,80,150,300


El archivo debe contener únicamente números enteros separados por comas o saltos de línea.

Ejemplo de archivo válido:
45,90,120,250,600


Ejemplo de archivo también válido:
45
90
120
250
600


Las mismas búsquedas cargadas desde el archivo se aplican a todas las estructuras seleccionadas.

---

## 8. Estructuras comparadas

El benchmark compara seis estructuras de datos:

1. Arreglo
2. Lista enlazada simple
3. BST
4. AVL
5. Splay
6. Red-Black

Todas las estructuras reciben la misma secuencia de inserción y el mismo conjunto de búsquedas para que la comparación sea justa.

---

## 9. Arreglo

El arreglo es una estructura lineal donde los elementos se almacenan de forma contigua.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad                              |
| --------- | ---------------------------------------- |
| Inserción | O(1) u O(n), dependiendo del crecimiento |
| Búsqueda  | O(n)                                     |
| Borrado   | O(n)                                     |

---

## 10. Lista enlazada simple

La lista enlazada simple está formada por nodos.
Cada nodo guarda un valor y una referencia al siguiente nodo.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad |
| --------- | ----------- |
| Inserción | O(n)        |
| Búsqueda  | O(n)        |
| Borrado   | O(n)        |

---

## 11. BST

El BST, o árbol binario de búsqueda, organiza los datos usando la siguiente regla:

* Los valores menores van a la izquierda.
* Los valores mayores van a la derecha.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Promedio | Peor caso |
| --------- | -------- | --------- |
| Inserción | O(log n) | O(n)      |
| Búsqueda  | O(log n) | O(n)      |
| Borrado   | O(log n) | O(n)      |

El peor caso puede ocurrir si el árbol queda muy desbalanceado.

---

## 12. AVL

El AVL es un árbol binario de búsqueda auto-balanceado.

Después de insertar o borrar elementos, el árbol puede realizar rotaciones para mantener su altura controlada.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad |
| --------- | ----------- |
| Inserción | O(log n)    |
| Búsqueda  | O(log n)    |
| Borrado   | O(log n)    |

---

## 13. Splay

El árbol Splay es un árbol binario de búsqueda autoajustable.

Cuando se accede a un nodo, este se mueve hacia la raíz mediante operaciones de reorganización llamadas splay.

Operaciones evaluadas:

* Inserción
* Búsqueda
* Borrado

Complejidades esperadas:

| Operación | Complejidad amortizada |
| --------- | ---------------------- |
| Inserción | O(log n)               |
| Búsqueda  | O(log n)               |
| Borrado   | O(log n)               |

---

## 14. Red-Black

El árbol Red-Black es un árbol binario de búsqueda balanceado mediante colores.

Cada nodo puede ser rojo o negro, y el árbol debe cumplir reglas de balance para evitar que su altura crezca demasiado.

En este proyecto, Red-Black se usa únicamente para:

* Inserción
* Búsqueda

No se mide el borrado en Red-Black.

Por eso, en los resultados de borrado debe aparecer:
N/A


Esto significa que la operación no aplica para esta estructura dentro del alcance del proyecto.

Complejidades esperadas:

| Operación | Complejidad |
| --------- | ----------- |
| Inserción | O(log n)    |
| Búsqueda  | O(log n)    |
| Borrado   | N/A         |

---

## 15. Resultados mostrados

La aplicación muestra una tabla comparativa con los resultados de cada estructura.

La tabla debe incluir:

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
* Tamaño en estructuras lineales.

En Red-Black, las columnas relacionadas con borrado deben mostrar `N/A`.

---

## 16. Exportación CSV

La aplicación permite exportar los resultados de la última corrida a un archivo CSV.

El CSV debe guardar la misma información que aparece en la tabla comparativa.

Formato sugerido del CSV:

Estructura,InsertTime,InsertComparisons,InsertO,SearchTime,SearchComparisons,SearchO,DeleteTime,DeleteComparisons,DeleteO,Height,Size,N,Seed,W,R
Array,1000,500,O(1),2000,1000,O(n),1500,700,O(n),N/A,1000,1000,42,1,3
SimpleLinkedList,2000,1200,O(n),3000,1500,O(n),2500,1400,O(n),N/A,1000,1000,42,1,3
BST,900,700,O(log n),800,600,O(log n),950,650,O(log n),15,N/A,1000,42,1,3
AVL,1000,800,O(log n),750,550,O(log n),1000,700,O(log n),10,N/A,1000,42,1,3
Splay,1100,850,O(log n),700,500,O(log n),1050,750,O(log n),12,N/A,1000,42,1,3
RedBlack,950,750,O(log n),720,530,O(log n),N/A,N/A,N/A,11,N/A,1000,42,1,3


Los valores anteriores son solo un ejemplo del formato.
Los valores reales dependen de cada corrida.

---

## 17. Flujo general del benchmark

El benchmark sigue este proceso:

1. Se leen los parámetros ingresados por el usuario.
2. Se genera la secuencia de inserción usando `N` y la semilla.
3. Se generan o cargan las búsquedas.
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

---

## 18. Consideraciones importantes

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
