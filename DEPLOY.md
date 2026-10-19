# 🚀 Guía de despliegue GRATIS de NexusCommerce

Esta guía te lleva, clic a clic, desde "tengo el código en GitHub" hasta "tengo una
demo pública que un reclutador puede abrir", **sin pagar nada y sin tarjeta de
crédito**. Está pensada para alguien que nunca ha desplegado nada: si un paso te
parece obvio, sáltalo; si algo no coincide exactamente con lo que ves en pantalla
(los paneles cambian de diseño a menudo), busca el botón con un nombre parecido.

> ⏱️ Tiempo estimado la primera vez: **60–90 minutos**.
>
> 🔐 Regla de oro: **ningún secreto (contraseñas, claves, cadenas de conexión) se
> escribe en el código ni se sube a GitHub.** Todos se pegan en los paneles de
> Render y Vercel. Guárdalos en un gestor de contraseñas o en un archivo local
> fuera del repositorio.

---

## Índice

0. [Arquitectura y por qué cuesta $0](#0-arquitectura-y-por-qué-cuesta-0)
1. [Cuentas necesarias](#1-cuentas-necesarias)
2. [Generar secretos](#2-generar-secretos)
3. [PostgreSQL en Neon](#3-postgresql-en-neon)
4. [MongoDB Atlas M0](#4-mongodb-atlas-m0)
5. [Backend en Render](#5-backend-en-render)
6. [Frontend en Vercel](#6-frontend-en-vercel)
7. [Datos demo](#7-datos-demo)
8. [CI con GitHub Actions](#8-ci-con-github-actions)
9. [Verificación end-to-end](#9-verificación-end-to-end)
10. [Solución de problemas](#10-solución-de-problemas)
11. [Mantenimiento](#11-mantenimiento)

---

## 0. Arquitectura y por qué cuesta $0

```
                        Navegador del reclutador
                                   │
                                   │  https://<tu-proyecto>.vercel.app
                                   ▼
        ┌──────────────────────────────────────────────────────┐
        │  VERCEL (Hobby) — Next.js 15                         │
        │                                                      │
        │  • Páginas (React Server Components) ──────────────┐ │
        │  • /api/*  → proxy (rewrite) ───────────────────┐  │ │
        └─────────────────────────────────────────────────┼──┼─┘
                                                          │  │ HTTPS
                     mismo dominio para el navegador:     │  │ (servidor a servidor)
                     la cookie de refresh es first-party  ▼  ▼
        ┌──────────────────────────────────────────────────────┐
        │  RENDER (free, Docker) — Spring Boot 3 / Java 21      │
        │  https://nexuscommerce-backend.onrender.com          │
        │  512 MB RAM · se duerme tras 15 min sin tráfico       │
        └───────────────┬─────────────────────────┬────────────┘
                        │ JDBC + TLS              │ mongodb+srv + TLS
                        ▼                         ▼
        ┌───────────────────────────┐  ┌───────────────────────────┐
        │ NEON (free) — PostgreSQL  │  │ MONGODB ATLAS (M0)        │
        │ AWS us-west-2 (Oregón)    │  │ AWS us-west-2 (Oregón)    │
        │ esquema `commerce`        │  │ base `nexuscatalog`       │
        │ (usuarios, pedidos, pagos)│  │ (catálogo de productos)   │
        └───────────────────────────┘  └───────────────────────────┘

        GITHUB ACTIONS — en cada push/PR: tests backend + lint/tests/build frontend
```

**¿Por qué el proxy `/api` en Vercel?** El frontend vive en `*.vercel.app` y el
backend en `*.onrender.com`: son *sitios distintos*. Si el navegador hablara
directamente con Render, la cookie de sesión (`nexus_refresh`) sería una *cookie de
terceros*, y Safari, Brave y casi todos los modos incógnito las bloquean → el usuario
"perdería la sesión" al recargar. Con el proxy, el navegador solo ve
`https://<tu-proyecto>.vercel.app/api/...`, la cookie es *first-party* y funciona en
todos los navegadores. Esto lo hace `frontend/next.config.ts` cuando existe la
variable `BACKEND_ORIGIN`.

**Todos en la misma región (Oregón / `us-west-2`)** para que cada consulta a la base
de datos tarde ~1–2 ms y no ~70 ms cruzando el continente.

### Qué ofrece gratis cada servicio

> Los límites cambian con el tiempo; revisa la página de precios de cada uno si algo
> no cuadra. Ninguno de estos planes pide tarjeta de crédito.

| Servicio | Qué aloja | Plan | Límites gratis relevantes | ¿Caduca? |
|---|---|---|---|---|
| **Vercel** | Frontend Next.js + proxy `/api` | Hobby | Uso personal / no comercial; ancho de banda y ejecuciones de funciones con cuota mensual holgada para un portafolio | No |
| **Render** | Backend Spring Boot (Docker) | Free | **512 MB RAM, 0,1 vCPU**; **se duerme tras 15 min sin tráfico** (despertar ≈ 1 min); **750 horas de instancia/mes** compartidas por *todos* los servicios free de tu workspace; minutos de build mensuales limitados | No (el servicio web free no caduca) |
| **Neon** | PostgreSQL | Free | ~0,5 GB de almacenamiento por proyecto; cuota mensual de horas de cómputo; **se suspende tras ~5 min sin consultas** y despierta en < 1 s | No |
| **MongoDB Atlas** | MongoDB (catálogo) | M0 | **512 MB** de almacenamiento, clúster compartido, sin backups automáticos, 1 clúster M0 por proyecto | No |
| **GitHub Actions** | CI | Free | **Minutos ilimitados en repositorios públicos** | No |

**¿Por qué Neon y no la base de datos Postgres de Render?** La base Postgres free de
Render **se elimina a los 30 días** de crearla. Neon no caduca, así que la demo sigue
viva meses después sin que tengas que tocar nada.

**¿Las 750 h alcanzan?** Un mes tiene como máximo 744 h. Un único servicio encendido
24/7 cabe justo; como el nuestro se duerme cuando nadie lo usa, consume mucho menos.
Solo tendrías problemas si creas *otros* servicios free en el mismo workspace de
Render (ver [§10](#10-solución-de-problemas)).

---

## 1. Cuentas necesarias

Crea estas cuentas (todas gratis). Donde se pueda, usa **"Continue with GitHub"**:
así no tienes otra contraseña que recordar y la conexión con tu repositorio es
inmediata.

| # | Servicio | URL de registro | Botón recomendado |
|---|---|---|---|
| 1 | GitHub | https://github.com/signup | (ya la tienes: el repo vive ahí) |
| 2 | Neon | https://console.neon.tech/signup | **Continue with GitHub** |
| 3 | MongoDB Atlas | https://www.mongodb.com/cloud/atlas/register | **Sign up with GitHub** (o Google) |
| 4 | Render | https://dashboard.render.com/register | **GitHub** |
| 5 | Vercel | https://vercel.com/signup | **Hobby** → **Continue with GitHub** |

Notas:

- En Vercel, cuando te pregunte el tipo de uso, elige **"I'm working on personal
  projects"** (plan Hobby).
- Atlas te hará unas preguntas de "onboarding" (lenguaje, objetivo…): responde
  cualquier cosa (p. ej. *Java*), no afectan a nada.
- El repositorio debe ser **público** para que GitHub Actions sea ilimitado y para
  que los reclutadores puedan ver el código.

---

## 2. Generar secretos

El backend necesita dos claves aleatorias. Esto es **exactamente** lo que exige el
código:

| Variable | Dónde se usa | Requisito exacto | Comando |
|---|---|---|---|
| `JWT_SECRET` | `JwtService` (firma HS256 de los access tokens) | Base64 estándar que decodifique a **≥ 32 bytes** (256 bits). Si es más corta, el arranque falla con `app.jwt.secret must be ... at least 256 bits (32 bytes)` | `openssl rand -base64 32` |
| `APP_ENCRYPTION_KEY` | `CryptoSupport` (AES-GCM de los datos de pago) | Base64 estándar que decodifique a **exactamente 16, 24 o 32 bytes**. Usamos 32 (AES-256) | `openssl rand -base64 32` |

`openssl rand -base64 32` genera 32 bytes aleatorios y los imprime en Base64:
**44 caracteres, terminando en `=`**, por ejemplo (¡no uses este!):
`q3Jx0V2n8yX9bQ1...=`.

### `JWT_SECRET`: lo genera Render por ti

En `render.yaml` está como `generateValue: true`: Render genera un valor aleatorio
"Base64 de 256 bits" (32 bytes), que cumple el requisito de `JwtService`. Si algún día
cambia, lo único que pasa es que todos los usuarios tienen que volver a iniciar
sesión. **No tienes que hacer nada.**

### `APP_ENCRYPTION_KEY`: la generas tú (y la guardas)

No la dejamos en manos de Render a propósito: los datos cifrados con ella viven en
**Neon**, fuera de Render. Si un día borras y recreas el servicio de Render con otra
clave, los datos de pago ya guardados serían ilegibles. Por eso la generas tú y
guardas una copia.

**Opción A — Windows PowerShell** (viene con Windows; ábrelo con Inicio → escribe
"PowerShell" → Enter). Pega estas tres líneas y pulsa Enter:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

**Opción B — Git Bash** (viene con Git para Windows; clic derecho en el escritorio →
"Open Git Bash here") o la terminal de macOS/Linux:

```bash
openssl rand -base64 32
```

Copia el resultado (44 caracteres, sin espacios ni saltos de línea al principio o al
final) y guárdalo como **`APP_ENCRYPTION_KEY`** en tu gestor de contraseñas.

> 💡 Si prefieres no usar `generateValue` para el JWT, genera otro valor igual y
> pégalo a mano; ambos comandos sirven para las dos claves.

---

## 3. PostgreSQL en Neon

### 3.1 Crear el proyecto

1. Entra en https://console.neon.tech.
2. Si es tu primera vez, verás directamente el formulario **"Create project"**. Si no,
   pulsa **New Project** (arriba a la derecha).
3. Rellena:
   - **Project name:** `nexuscommerce`
   - **Postgres version:** deja la que venga por defecto (16 o superior; el proyecto
     se prueba con 16).
   - **Cloud provider:** **AWS**.
   - **Region:** **AWS US West 2 (Oregon)** ← importante, es la misma que Render.
   - **Database name** (si te lo pide): deja `neondb`.
4. Pulsa **Create project**.

No hace falta crear tablas ni ejecutar SQL: al arrancar, el backend ejecuta las
migraciones de **Flyway** (V1 a V10) y crea solo el esquema `commerce` con todas sus
tablas, vistas, procedimiento y datos de roles.

### 3.2 Obtener los datos de conexión

1. En el panel del proyecto (**Project dashboard**) pulsa el botón **Connect**.
2. En la ventana que se abre:
   - **Branch:** `main` (o `production`, según cómo la llame Neon).
   - **Database:** `neondb`.
   - **Role:** `neondb_owner`.
   - **Connection pooling:** **DESACTÍVALO** (interruptor en *off*). El host NO debe
     contener `-pooler`. Explicación abajo.
3. Verás una cadena como esta (los valores son inventados):

   ```
   postgresql://neondb_owner:AbCdEf123456@ep-cool-river-a1b2c3d4.us-west-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
   ```

   Pulsa **Show password** si aparece oculta, y cópiala completa a un bloc de notas
   (que NO esté dentro de la carpeta del proyecto).

### 3.3 Convertirla al formato que usa Java (JDBC)

Separa la cadena en piezas:

```
postgresql:// neondb_owner : AbCdEf123456 @ ep-cool-river-a1b2c3d4.us-west-2.aws.neon.tech / neondb ?sslmode=require...
              └─ usuario ─┘ └ contraseña ┘ └──────────────────── host ──────────────────┘ └─ base ┘
```

y construye estos **tres valores** para Render:

| Variable en Render | Valor (con el ejemplo de arriba) |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-cool-river-a1b2c3d4.us-west-2.aws.neon.tech/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | `AbCdEf123456` |

Reglas:

- La URL empieza por **`jdbc:postgresql://`** (no `postgresql://`).
- **No** pongas usuario ni contraseña dentro de la URL: van en sus propias variables
  (así una contraseña con caracteres raros no rompe nada).
- Mantén **`?sslmode=require`** al final (Neon solo acepta conexiones cifradas). El
  parámetro `channel_binding` puedes omitirlo.

### 3.4 ¿Host directo o con *pooler*? → **Directo**

Neon ofrece dos hosts: el **directo** (`ep-xxx.us-west-2...`) y el **pooler**
(`ep-xxx-pooler.us-west-2...`, un PgBouncer en modo *transaction*). Usamos el
**directo** porque:

- **Flyway** toma un *advisory lock* de sesión para que dos instancias no migren a la
  vez. En modo *transaction* de PgBouncer cada sentencia puede ir a una conexión
  distinta, ese bloqueo no es fiable y las migraciones pueden fallar o colgarse.
- El backend ya tiene su propio pool (HikariCP), limitado a **5 conexiones** en el
  perfil `demo`; el límite de conexiones directas de Neon free está muy por encima.

(El pooler solo tendría sentido con cientos de conexiones simultáneas, p. ej. con
funciones serverless; no es nuestro caso.)

---

## 4. MongoDB Atlas M0

### 4.1 Crear el clúster gratuito

1. Entra en https://cloud.mongodb.com. Si te pide crear una **Organization** y un
   **Project**, ponles cualquier nombre (p. ej. `nexuscommerce`).
2. Pulsa **Create** (o **Build a Database** / **+ Create cluster**).
3. Elige la tarjeta **M0 — Free**.
4. Configura:
   - **Name:** `Cluster0` (o el que quieras).
   - **Provider:** **AWS**.
   - **Region:** **Oregon (us-west-2)**. Si no aparece para M0, elige la región
     gratuita de EE. UU. más cercana (p. ej. N. Virginia `us-east-1`); funcionará,
     solo con algo más de latencia.
   - Desmarca **"Preload sample dataset"** si aparece (no lo necesitamos).
5. Pulsa **Create Deployment**. Tarda 1–3 minutos.

### 4.2 Crear el usuario de base de datos

Al terminar se abre **"Connect to Cluster0"** (si no, ve a **Security → Database
Access → Add New Database User**):

1. **Username:** `nexus_app`.
2. **Password:** pulsa **Autogenerate Secure Password** y luego **Copy**. Guárdala.
   (La contraseña autogenerada solo tiene letras y números: así no hay que
   "escapar" caracteres especiales en la URL.)
3. Rol: **Read and write to any database** (el valor por defecto).
4. Pulsa **Create Database User**.

### 4.3 Permitir conexiones desde Render (Network Access)

1. Menú izquierdo → **Security → Network Access** → **+ Add IP Address**.
2. Pulsa **Allow Access from Anywhere**. Se rellena `0.0.0.0/0`.
3. **Comment:** `Render free (sin IP fija)`. Pulsa **Confirm**.
4. Espera a que el estado pase de *Pending* a **Active** (~1 min).

**¿Por qué 0.0.0.0/0?** El plan gratuito de Render **no tiene IP de salida fija**: la
IP desde la que se conecta tu backend puede cambiar en cada despliegue o reinicio, así
que no hay una IP concreta que poner en la lista blanca. La protección real es el
usuario + contraseña fuerte y el TLS obligatorio de Atlas. (Las IP fijas de salida son
una función de pago en Render.)

### 4.4 Obtener la cadena de conexión SRV

1. Menú izquierdo → **Database → Clusters** → en tu clúster pulsa **Connect**.
2. Elige **Drivers**. **Driver:** *Java*, cualquier versión.
3. Copia la cadena. Se parece a:

   ```
   mongodb+srv://nexus_app:<db_password>@cluster0.ab1cd.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0
   ```

4. Haz **dos cambios**:
   - Sustituye `<db_password>` (incluidos `<` y `>`) por la contraseña del paso 4.2.
   - Inserta el nombre de la base **`nexuscatalog`** justo después de `.net/` y antes
     de `?`.

   Resultado (valor de **`SPRING_DATA_MONGODB_URI`**):

   ```
   mongodb+srv://nexus_app:TuPassword123@cluster0.ab1cd.mongodb.net/nexuscatalog?retryWrites=true&w=majority&appName=Cluster0
   ```

No hace falta crear la base ni las colecciones: se crean solas con los datos demo.

---

## 5. Backend en Render

El archivo [`render.yaml`](render.yaml) de la raíz del repo es un **Blueprint**: le
dice a Render exactamente qué crear (un único servicio web Docker gratuito en Oregón)
y qué variables pedirte.

### 5.1 Crear el servicio desde el Blueprint

1. Asegúrate de que la rama `main` de GitHub contiene `render.yaml` (haz *push* antes).
2. Entra en https://dashboard.render.com.
3. Arriba a la derecha: **+ New** → **Blueprint**.
4. Si es la primera vez, pulsa **Connect GitHub** / **Configure account**, autoriza a
   Render y dale acceso al repositorio **NexusCommerce** (puedes elegir "Only select
   repositories").
5. En la lista, junto a `CamiloRaphaelZuletaWolff/NexusCommerce`, pulsa **Connect**.
6. **Blueprint Name:** `nexuscommerce`. **Branch:** `main`. Render lee `render.yaml`
   y muestra que va a crear **1 servicio**: `nexuscommerce-backend` (Web Service,
   Docker, Free).
7. Debajo aparecen las variables marcadas `sync: false`. Rellénalas:

   | Variable | Valor |
   |---|---|
   | `SPRING_DATASOURCE_URL` | La URL JDBC del [§3.3](#33-convertirla-al-formato-que-usa-java-jdbc) |
   | `SPRING_DATASOURCE_USERNAME` | `neondb_owner` |
   | `SPRING_DATASOURCE_PASSWORD` | La contraseña de Neon |
   | `SPRING_DATA_MONGODB_URI` | La cadena SRV del [§4.4](#44-obtener-la-cadena-de-conexión-srv) |
   | `APP_ENCRYPTION_KEY` | La clave del [§2](#2-generar-secretos) |
   | `DEMO_ADMIN_PASSWORD` | Contraseña **privada** para `admin@nexus.local` (mínimo 12 caracteres; genera una con el mismo comando del [§2](#2-generar-secretos)). **No la publiques.** Si la dejas vacía, la demo no tiene admin. |
   | `APP_CORS_ALLOWED_ORIGINS` | La URL que **tendrá** tu frontend, p. ej. `https://nexuscommerce.vercel.app` (sin `/` final). Si aún no la sabes, pon esa; la corregirás en el [§6.4](#64-cerrar-el-círculo-cors-en-render). |

   `JWT_SECRET` no aparece porque Render lo genera solo; `SPRING_PROFILES_ACTIVE=demo`,
   `AUTH_COOKIE_SECURE`, `AUTH_COOKIE_SAMESITE` y `JAVA_TOOL_OPTIONS` ya vienen fijadas
   en el archivo.

8. Pulsa **Deploy Blueprint** (o **Apply**).

> ⚠️ El perfil `demo` **no tiene valores por defecto** para secretos ni bases de
> datos: si te olvidas de una variable, el arranque falla a propósito con un bloque
> `APPLICATION FAILED TO START` en los logs que nombra lo que falta, p. ej.
> `Failed to configure a DataSource: 'url' attribute is not specified` (falta
> `SPRING_DATASOURCE_URL`) o `app.encryption.key ... must not be blank`. Es mejor que
> arrancar con una clave de desarrollo pública.

### 5.2 Seguir el despliegue y leer los logs

1. Ve a **Dashboard** → pulsa **nexuscommerce-backend**.
2. Pestaña **Events**: verás *Deploy started*. Pulsa sobre el deploy para ver el log
   de **build**.
3. Pestaña **Logs**: es el log de la aplicación en ejecución (lo que imprime Spring).
   Usa el cuadro de búsqueda para filtrar, p. ej. `ERROR` o `Flyway`.

Qué esperar en el **primer** despliegue:

| Fase | Duración aprox. | Qué verás en el log |
|---|---|---|
| Build Docker (Maven descarga dependencias y compila) | 5–10 min | `mvn ... package`, `BUILD SUCCESS`, `Pushing image` |
| Arranque de Spring en 0,1 vCPU | 2–5 min (la CPU es mínima; ten paciencia) | `Picked up JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=60.0 ...` (normal, no es un error), `Successfully applied 10 migrations to schema "commerce"`, `Started NexusCommerceApplication in ... seconds`, y justo después `Seeded demo user admin@nexus.local [ADMIN]` (o el aviso `DEMO_ADMIN_PASSWORD is not set` si la dejaste vacía), `Seeded demo stores`, `Seeded demo catalog: 6 products`, `Seeded inventory for 6 products` |
| Health check | segundos | El estado pasa a **Live** (verde) |

Los siguientes despliegues son más rápidos y las migraciones/seeds ya no se repiten
(son idempotentes).

### 5.3 Verificar que funciona

La URL pública está arriba a la izquierda del servicio, p. ej.
`https://nexuscommerce-backend.onrender.com` (si el nombre ya estaba ocupado, Render
añade un sufijo como `-ab12`; usa **tu** URL en todo lo que sigue).

Desde PowerShell, Git Bash o cualquier terminal:

```bash
curl https://nexuscommerce-backend.onrender.com/actuator/health
```

Respuesta esperada:

```json
{"status":"UP","groups":["liveness","readiness"]}
```

(En el perfil `demo` no se muestran los detalles internos de la base de datos, a
propósito.) Prueba también un endpoint público de la API:

```bash
curl "https://nexuscommerce-backend.onrender.com/api/stores"
```

Y abre en el navegador la documentación interactiva (Swagger UI):

```
https://nexuscommerce-backend.onrender.com/swagger-ui.html
```

> En PowerShell 5.1, `curl` es un alias de `Invoke-WebRequest`; si ves un formato
> raro, usa `curl.exe` en su lugar.

---

## 6. Frontend en Vercel

### 6.1 Importar el proyecto

1. Entra en https://vercel.com/new (o **Add New… → Project** desde el dashboard).
2. En **Import Git Repository**, si no ves tu repo pulsa **Adjust GitHub App
   Permissions** y da acceso a `NexusCommerce`.
3. Junto a `NexusCommerce` pulsa **Import**.

### 6.2 Configurar

En la pantalla **Configure Project**:

1. **Project Name:** `nexuscommerce` → tu URL será `https://nexuscommerce.vercel.app`
   (si está ocupada, Vercel usará otra; anótala).
2. **Framework Preset:** **Next.js** (lo detecta solo).
3. **Root Directory:** pulsa **Edit** → selecciona la carpeta **`frontend`** →
   **Continue**. ← imprescindible, el repo es un monorepo.
4. **Build and Output Settings:** no toques nada (`npm run build`, `npm install`).
   La versión de Node (22) la fija `frontend/package.json` (`"engines"`) y
   `frontend/.nvmrc`.
5. Despliega **Environment Variables** y añade estas tres (botón **Add** tras cada una):

   | Key | Value | Para qué |
   |---|---|---|
   | `BACKEND_ORIGIN` | `https://nexuscommerce-backend.onrender.com` | Destino del proxy `/api/*` (solo servidor; sin `/` final, sin `/api`) |
   | `NEXT_PUBLIC_API_BASE_URL` | `/api` | El navegador llama a su propio dominio → cookie first-party |
   | `API_INTERNAL_BASE_URL` | `https://nexuscommerce-backend.onrender.com/api` | Las páginas que se renderizan en el servidor llaman directo a Render |

   Sustituye `nexuscommerce-backend.onrender.com` por **tu** URL de Render.

6. Pulsa **Deploy**. Tarda 1–3 minutos. Al terminar verás confeti y una captura de
   tu web. Pulsa **Continue to Dashboard**.

> ⚠️ `NEXT_PUBLIC_*` y las reglas del proxy se "congelan" **en el build**. Si cambias
> cualquiera de estas variables más tarde, tienes que **volver a desplegar**:
> pestaña **Deployments** → los tres puntos `⋯` del último → **Redeploy**.

### 6.3 (Opcional) Región de las funciones

Para que las páginas renderizadas en servidor estén cerca de Render: **Settings →
Functions → Function Region** → elige **Portland, USA (West) – pdx1** → **Save** y
vuelve a desplegar.

### 6.4 Cerrar el círculo: CORS en Render

Aunque el navegador solo habla con Vercel, el proxy reenvía la cabecera `Origin`, y
Spring la valida contra `APP_CORS_ALLOWED_ORIGINS`. Debe coincidir **exactamente** con
la URL de producción de Vercel:

1. En Vercel, copia la URL de **Domains** del proyecto (p. ej.
   `https://nexuscommerce.vercel.app`). Usa la de producción, no la larga de cada
   deploy (`nexuscommerce-git-main-xxxx.vercel.app`).
2. En Render → **nexuscommerce-backend** → **Environment**.
3. Edita `APP_CORS_ALLOWED_ORIGINS` → pega la URL **sin barra final** →
   **Save, rebuild, and deploy** (o **Save and deploy**).
4. Si quieres permitir varios orígenes, sepáralos por comas:
   `https://nexuscommerce.vercel.app,https://midominio.com`.

---

## 7. Datos demo

Con `SPRING_PROFILES_ACTIVE=demo`, al arrancar se ejecutan los *seeders* de
`backend/src/main/java/com/nexuscommerce/bootstrap/` (los mismos que en desarrollo):

- **3 usuarios:**

  | Rol | Email | Contraseña | Qué puede hacer |
  |---|---|---|---|
  | `CUSTOMER` | `customer@nexus.local` | `Password123!` (pública) | Comprar, carrito, checkout, pedidos, cuenta |
  | `STORE_MANAGER` | `manager@nexus.local` | `Password123!` (pública) | Lo anterior + dashboard y analíticas **de sus tiendas**, crear productos |
  | `ADMIN` | `admin@nexus.local` | `DEMO_ADMIN_PASSWORD` (**privada**) | Todo, incluidas las analíticas de toda la plataforma y el listado de pedidos de todos los clientes |

  El admin **no** usa la contraseña pública: en una demo abierta, publicar la del
  admin significaría que cualquiera es admin. Su contraseña sale de la variable
  `DEMO_ADMIN_PASSWORD` de Render; si no está definida no se crea (y un admin creado
  por un despliegue anterior queda **desactivado**). Para cambiarla, edita la variable
  y redespliega: al arrancar se re-sincroniza y se cierran sus sesiones abiertas.

- **2 tiendas:** *Nexus Electronics* y *Nexus Fashion* (gestionadas por el manager).
- **Catálogo en MongoDB:** categorías, marcas y **6 productos** (portátil, móvil,
  auriculares, camiseta, chaqueta, zapatillas).
- **Inventario en PostgreSQL:** 100 unidades de cada producto.

Los seeders son **idempotentes**: solo crean lo que falta, así que reiniciar el
servicio no duplica nada. Para borrar todo y empezar de cero, ver
[§11](#11-mantenimiento).

> Las credenciales de cliente y manager son públicas a propósito (para reclutadores).
> No reutilices nunca `Password123!` en nada real.

---

## 8. CI con GitHub Actions

El workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) se ejecuta en cada
*push* a `main` y en cada *pull request*. Si haces dos *push* seguidos, cancela la
ejecución anterior (`concurrency`).

| Job | Directorio | Pasos |
|---|---|---|
| **backend** | `backend/` | JDK Temurin 21 con caché de Maven → `mvn -B verify`: tests unitarios + ArchUnit (Surefire) **y** `BackendIntegrationIT` (Failsafe) con PostgreSQL 16 y MongoDB 7 reales en Docker vía Testcontainers. Si falla, sube los informes como *artifact*. |
| **frontend** | `frontend/` | Node 22 (de `.nvmrc`) con caché de npm → `npm ci` → `npm run lint` → `npx tsc --noEmit` → `npm test` (Vitest) → `npm run build` con `NEXT_PUBLIC_API_BASE_URL=/api` |

### Cómo leer un fallo

1. En GitHub, pestaña **Actions** → pulsa la ejecución con la ❌ roja.
2. Pulsa el job que falló (**backend** o **frontend**) → se despliega el paso con ❌.
   Lee desde abajo hacia arriba: el error real suele estar unas líneas antes del final.
3. Backend: busca `Tests run: ... Failures:` y `[ERROR]`. Para el detalle completo,
   baja al final de la página del run → **Artifacts** → `backend-test-reports`
   (ZIP con los informes de Surefire/Failsafe).
4. Frontend: los errores de ESLint y TypeScript indican archivo y línea
   (`components/x.tsx:12:5`).
5. Para reproducirlo en local: `cd backend && mvn verify` (necesita Docker Desktop
   abierto) o `cd frontend && npm ci && npm run lint && npx tsc --noEmit && npm test`.

### Badge para el README

Ya está añadido al principio del `README.md`:

```markdown
[![CI](https://github.com/CamiloRaphaelZuletaWolff/NexusCommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/CamiloRaphaelZuletaWolff/NexusCommerce/actions/workflows/ci.yml)
```

Se pone verde tras la primera ejecución correcta en `main`.

---

## 9. Verificación end-to-end

Haz esta lista en una **ventana de incógnito** (y, si puedes, también en Safari o en
el móvil):

- [ ] `curl https://<tu-backend>.onrender.com/actuator/health` → `{"status":"UP",...}`.
- [ ] Abre `https://<tu-proyecto>.vercel.app`. Si el backend dormía, aparece arriba el
      aviso **"Waking up the server (free hosting tier, this can take 2–3 minutes)…"** y
      desaparece solo; después la página se rellena sin recargar.
- [ ] La home muestra las tiendas y `/catalog` muestra los 6 productos.
- [ ] **Login** con `customer@nexus.local` / `Password123!` → ves tu nombre en la
      barra superior.
- [ ] **Cookie correcta:** F12 → **Application** (Chrome) / **Almacenamiento**
      (Firefox) → **Cookies** → `https://<tu-proyecto>.vercel.app` → existe
      `nexus_refresh` con **HttpOnly ✓**, **Secure ✓**, **SameSite Lax**,
      **Path `/api/auth`**. Su dominio es el de **Vercel**, no el de Render.
- [ ] **La sesión sobrevive a una recarga:** pulsa F5 → sigues logueado (el frontend
      pide un nuevo access token a `/api/auth/refresh` usando la cookie).
- [ ] F12 → **Network**: todas las peticiones van a `https://<tu-proyecto>.vercel.app/api/...`
      (ninguna directamente a `onrender.com`) y no hay errores CORS en la consola.
- [ ] Añade un producto al carrito → **Checkout** → dirección cualquiera, país de 2
      letras (`US`), tarjeta de prueba `4111111111111111`, caducidad futura `12/30`,
      CVV `123` → el pedido se crea y aparece en **Orders**.
- [ ] Cierra sesión → entra con `manager@nexus.local` → **Dashboard** muestra
      analíticas de **sus** tiendas (incluido el pedido anterior); el listado de
      pedidos de todos los clientes solo lo ve el admin.
- [ ] El botón de cuentas demo del login **no** ofrece "Admin", y
      `admin@nexus.local` / `Password123!` devuelve *Incorrect email or password*.
- [ ] Fuerza bruta: 5 contraseñas incorrectas seguidas para la misma cuenta → la
      6.ª petición devuelve **429** ("Too many attempts") durante 15 min.
- [ ] Swagger: `https://<tu-backend>.onrender.com/swagger-ui.html` carga y el servidor
      que muestra empieza por **https**.

---

## 10. Solución de problemas

### El servicio de Render se reinicia solo / "Out of memory (used over 512Mi)"

- En **Events** verás *"Instance failed: ran out of memory"*.
- Comprueba en **Environment** que `JAVA_TOOL_OPTIONS` tiene
  `-XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:+ExitOnOutOfMemoryError`
  y que en los logs aparece `Picked up JAVA_TOOL_OPTIONS: ...`.
- Si aún así se queda sin memoria, baja a `-XX:MaxRAMPercentage=50.0` (≈256 MB de
  heap). **No** lo subas a 75 %: la memoria que no es heap (metaspace, threads, código
  JIT) no cabría y el contenedor moriría por OOM sin aviso.
- `java.lang.OutOfMemoryError: Java heap space` en los logs (en vez del kill del
  contenedor) significa lo contrario: el heap es pequeño para la carga; sube a 65 %.

### Errores de Flyway al arrancar

- `Unable to obtain connection` / `Connection refused` / `timeout`: revisa
  `SPRING_DATASOURCE_URL` (empieza por `jdbc:postgresql://`, host **sin** `-pooler`,
  termina en `?sslmode=require`), usuario y contraseña.
- `password authentication failed`: contraseña mal copiada. En Neon → **Roles** →
  `neondb_owner` → **Reset password**, y actualízala en Render.
- `Validate failed: Migrations have failed validation` / `checksum mismatch`: alguien
  modificó un archivo `V*.sql` ya aplicado. Nunca edites migraciones aplicadas; crea
  una `V11__...sql`. Si es la demo y no te importan los datos, resetea (ver §11).
- `Schema-validation: missing table [...]` (Hibernate): las migraciones no se
  aplicaron; busca el primer error de Flyway más arriba en el log.

### No conecta con MongoDB Atlas

- `Timed out after 30000 ms while waiting to connect` / `MongoSocketReadException`:
  casi siempre es **Network Access**. Comprueba que `0.0.0.0/0` está **Active**.
- `Authentication failed` / `bad auth`: usuario/contraseña de la cadena SRV erróneos o
  dejaste `<db_password>` literal. Si tu contraseña tiene `@ : / ? # %`, cámbiala por
  una autogenerada (solo alfanumérica).
- Comprueba que pusiste `/nexuscatalog` antes del `?`.

### Errores CORS / 403 en login

- Consola del navegador: *"blocked by CORS policy"* o las peticiones POST devuelven
  **403**: `APP_CORS_ALLOWED_ORIGINS` no coincide **exactamente** con la URL de Vercel
  (`https://`, sin `/` final, mismo subdominio). Corrígelo y redeploy (§6.4).
- Las URLs de *preview* de Vercel (`...-git-rama-...vercel.app`) no están en la lista:
  prueba siempre en la URL de producción.

### Tras recargar la página se pierde la sesión

- Revisa la cookie (§9). Si **no** existe:
  - `NEXT_PUBLIC_API_BASE_URL` debe ser exactamente `/api` y `BACKEND_ORIGIN` debe
    existir; si cambiaste algo, **Redeploy** en Vercel (se fijan en el build).
  - En **Network**, si las peticiones van a `onrender.com` directamente, el build no
    tenía `NEXT_PUBLIC_API_BASE_URL=/api`.
- Si existe pero no se envía: debe tener **Path `/api/auth`**. `AUTH_COOKIE_DOMAIN` no
  debe estar definida en Render.

### Las páginas salen vacías o el aviso de "despertando" no desaparece

- Es el **arranque en frío**: Render free duerme tras 15 min sin tráfico y tarda
  2–3 min (a veces más) en despertar. El aviso hace polling y, cuando el backend
  responde, recarga los datos solo.
- Si tras 5 minutos el aviso se va pero sigue sin datos: mira **Logs** en Render (¿el
  arranque falló?) y que `API_INTERNAL_BASE_URL` y `BACKEND_ORIGIN` apunten a tu URL
  de Render.
- Si molesta mucho, ver "mantener despierto" en §11 (con su coste en horas).

### "Free instance hours exhausted" / el servicio se suspende a final de mes

- Las **750 h/mes** se comparten entre **todos** los servicios free del workspace.
  Ve a **Workspace Settings → Billing / Usage** para ver el consumo.
- Si tienes otros servicios free (de otros proyectos) encendidos o con keep-alive,
  suspéndelos (**Settings → Suspend Web Service**) o muévelos a otro workspace.

### El build de Render falla

- `COPY failed` / `pom.xml not found`: `dockerContext` debe ser `./backend` y
  `dockerfilePath` `./backend/Dockerfile` (ya vienen así en `render.yaml`).
- El build se queda sin minutos de pipeline: espera al mes siguiente o reduce
  despliegues (desactiva auto-deploy temporalmente).

---

## 11. Mantenimiento

### Despliegue automático

- **Render** (`autoDeploy: true`) y **Vercel** vuelven a desplegar solos con cada
  *push* a `main`. Haz *push* → espera ~5–10 min (Render) y ~2 min (Vercel).
- Las nuevas migraciones (`V11__...sql`, etc.) se aplican solas al arrancar.
- Si cambias `render.yaml`, Render sincroniza el Blueprint (variables `sync: false`
  ya rellenadas se conservan).

### Neon se suspende solo

Tras ~5 minutos sin consultas, Neon apaga el cómputo (*scale to zero*) y lo enciende
en menos de un segundo con la siguiente consulta. Es normal y ahorra tu cuota mensual
de horas de cómputo. Puedes ver el consumo en Neon → **Billing / Usage**.

### Resetear los datos de la demo

Borra **ambas** bases a la vez (los productos de Mongo y el inventario de Postgres
están enlazados por UUID):

1. **Neon** → tu proyecto → **SQL Editor** → ejecuta:

   ```sql
   DROP SCHEMA commerce CASCADE;
   ```

   (El historial de Flyway vive dentro de `commerce`, así que también se borra.)
2. **Atlas** → **Database → Clusters** → **Browse Collections** → pasa el ratón sobre
   la base `nexuscatalog` → icono de papelera → escribe `nexuscatalog` → **Drop**.
3. **Render** → **nexuscommerce-backend** → **Manual Deploy → Restart service** (o
   *Deploy latest commit*). Al arrancar, Flyway recrea el esquema y los seeders
   vuelven a crear usuarios, tiendas, catálogo e inventario.

### Rotar secretos

- `JWT_SECRET`: Render → **Environment** → edita el valor (o genera uno nuevo con
  `openssl rand -base64 32`) → guarda. Efecto: todos tienen que volver a iniciar
  sesión.
- `APP_ENCRYPTION_KEY`: **no la cambies** sin resetear los datos antes: los pagos ya
  cifrados dejarían de poder leerse.
- `DEMO_ADMIN_PASSWORD`: edítala en Render y redespliega; al arrancar se aplica la
  nueva contraseña y se revocan las sesiones del admin.
- Contraseñas de Neon/Atlas: cámbialas en su panel y actualiza la variable en Render.

### (Opcional) Mantener el backend despierto

Un monitor gratuito (p. ej. cron-job.org o UptimeRobot) que llame a
`https://<tu-backend>.onrender.com/actuator/health` cada 10–14 min evita el arranque en
frío. **Coste:** el servicio estaría encendido 24/7 (~744 h/mes), casi todas las 750 h
gratuitas del workspace, y Neon tampoco se suspendería nunca (más horas de cómputo).
Para un portafolio suele bastar con el aviso de "despertando" y abrir la demo un
minuto antes de una entrevista.
