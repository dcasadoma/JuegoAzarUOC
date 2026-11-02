# 🎰 Lucky UOC Casino - Juego de Azar en Android

Repositorio correspondiente al **Producto 1** del módulo *Desarrollo de aplicaciones móviles nativas* (UOC).

## 📱 Descripción del Proyecto
Aplicación nativa para Android desarrollada en **Java** que simula una máquina tragaperras de 3 rodillos. El jugador dispone de un saldo inicial de monedas que varía según los resultados obtenidos en cada tirada, registrando el historial y las mejores puntuaciones de forma local.

## 🛠️ Características y Tecnologías Utilizadas
* **Interfaz y Navegación:** Diseño adaptable (`ConstraintLayout`), pantalla de bienvenida con ranking, barra de acción (`ActionBar`) con menú de opciones y animaciones gráficas mediante `ObjectAnimator`.
* **Recursos Estáticos:** Uso de gráficos vectoriales (`VectorDrawable`) optimizados en `res/drawable`.
* **Lógica de Azar:** Generación aleatoria mediante `SecureRandom` desacoplada en su propia clase de negocio (`SlotMachineGame`).
* **Persistencia de Datos:** Base de datos local **SQLite** (`SQLiteOpenHelper`) para almacenar el saldo del jugador y el histórico de partidas.
* **Asincronía:** Implementación de **RxJava 3** y **RxAndroid** (`Schedulers.io()` y `AndroidSchedulers.mainThread()`) para realizar las consultas y escrituras en la base de datos sin bloquear el hilo principal.

## 🚀 Requisitos y Ejecución
1. Clonar este repositorio o descargarlo como archivo `.zip`.
2. Abrir el proyecto en **Android Studio**.
3. Sincronizar las dependencias de **Gradle** y ejecutar sobre un emulador o dispositivo físico con **Android 7.0 (API 24)** o superior.

---
**Autor:** Daniel Casado
