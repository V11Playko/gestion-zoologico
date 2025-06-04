# Gestión Zoológico
Aplicación backend para la gestión integral de un zoológico, que permite administrar zonas, especies, animales y usuarios. Incluye un sistema de comentarios anidados para cada animal, facilitando la comunicación y seguimiento de observaciones por parte de empleados.

# Instalación y configuración

Antes de empezar, asegúrate de tener instalados y configurados los siguientes elementos:

- Java 17
- Gradle 8.5
- Base de datos PostgreSQL (con las variables de entorno `USER` y `PASSWORD` ya configuradas).

## 🛠️ Pasos para configurar el entorno local

1. Clona este repositorio en tu máquina local.
2. Importa el proyecto en tu IDE favorito.
3. Instala las dependencias del proyecto usando Gradle.
4. Crea las bases de datos PostgreSQL necesaria (`gestion-zoologico`).
5. Configura las variables de entorno necesarias para la base de datos.

### 🔧 Configuración de variables de entorno para PostgreSQL

En tu IDE (ej. IntelliJ IDEA):

1. Dirígete a `Run/Debug Configurations`.
2. Selecciona la configuración del microservicio actual (por ejemplo, `gestion-zoologico`).
3. En la sección **Environment Variables**, agrega las siguientes:

| Variable | Descripción |
|----------|-------------|
| `USER`   | Usuario de tu base de datos PostgreSQL. *(Lo puedes encontrar en PgAdmin en `PostgreSQL -> Properties -> Connection -> Username`)* |
| `PASSWORD` | Contraseña de tu base de datos PostgreSQL. |

---

### 🔐 Configuración de variables JWT

En la misma sección de **Environment Variables**, añade también:

| Variable           | Descripción                                                                                                                                |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `KEYSECRET_SECURITY`| Clave secreta para firmar y validar los JWT. Usa una cadena segura aleatoria. Puedes generar una con el comando `openssl rand -base64 64` desde Git Bash. |
| `EXPIRE_MS_SECURITY`| Tiempo de expiración del token en milisegundos (por ejemplo, `86400000` equivale a 1 día).                                                  |

#### 💡 Ejemplo de valores:
```env
KEYSECRET_SECURITY=FcdKmEV6u/EzvXzFEDg4xsR/zivknMrKR9GoluF3fFG8Zi9Ybw37TEVwToaVIhBjm3vaiE0L+RD+hyPwid9BcA==
EXPIRE_MS_SECURITY=86400000
```

## 📚 Documentación de la API

- **Gestión-Zoológico**: [http://localhost:8091/swagger-ui/index.html#/](http://localhost:8091/swagger-ui/index.html#/)

---

## 📝 Licencia

Este proyecto está licenciado bajo la **Apache License**. Consulta el archivo [`LICENSE`](./LICENSE) para más información.

---

## 💬 Comentarios

Si tienes algún comentario sobre el repositorio, por favor házmelo saber para poder mejorar 🙂

📫 **Cómo contactarme**: heinnervega20@gmail.com


