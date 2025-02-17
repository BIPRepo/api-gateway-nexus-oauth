# 🚀 API Gateway con Nexus e Keycloak

Un repository per configurare e avviare un API Gateway basato su Spring Cloud Gateway con autenticazione OAuth tramite Keycloak al fine di effettuare autenticazione ed autorizzazione con Nexus Repository Manager.

## 📋 Prerequisiti

Assicurarsi di avere installato:

- 🐧 **WSL2** (o Linux nativo)
- 🐳 **Docker**
- 📦 **Docker Compose**
- 🌐 Accesso ai registry di immagini pubblici

## 📥 Clonazione del Repository

Clonare il repository da GitHub:

```sh
 git clone https://github.com/BIPRepo/api-gateway-nexus-oauth.git
 cd api-gateway-nexus-oauth
```

Il repository contiene:

1. **API Gateway** (Spring Cloud Gateway)
2. **Docker Compose** con:
   - 📌 Nexus Repository Manager
   - 🔑 Keycloak

## 🔧 Configurazione

### 🛠️ Impostare i permessi delle cartelle

Assicurarsi di avere i privilegi di scrittura nelle seguenti directory:

```sh
sudo chmod 777 -R <repo_dir>/compose/docker-data/nexus_dir
sudo chmod 777 -R <repo_dir>/compose/docker-data/keycloak
```

*(Nota: i permessi 777 sono troppo elevati per un ambiente di produzione, usarli solo per test.)*

### 🏠 Modifica del file hosts

Aggiungere le seguenti righe al file `/etc/hosts` per la risoluzione dei nomi:

```sh
127.0.0.1  keycloak nexus-test-ld
```

## 🚀 Avvio dei Servizi

### 🔨 Compilazione dell'API Gateway

Per compilare eseguire il seguente comando:

```sh
cd <repo_dir>
docker run -it --rm --name build-image -v "$(pwd)":/usr/src/test -w /usr/src/test maven:3.9.5 mvn clean install -DskipTests
```

### 📦 Avvio dei Container

Avviare Nexus e Keycloak:

```sh
cd <repo_dir>/compose/
docker compose up -d
```

Attendere che i servizi siano operativi controllando i log dei container.

### 🚦 Avvio dell'API Gateway

```sh
cd <repo_dir>/target/
java -jar api-gateway-0.0.1-SNAPSHOT.jar
```

## 🔑 Accesso a Nexus

1. Aprire un browser e accedere a:
   ```
   http://localhost:8082
   ```
2. Effettuare l'autenticazione OAuth inserendo:
   - **Username:** `myuser`
   - **Password:** `admin`
3. Dopo l'autenticazione, si verrà reindirizzati a Nexus Repository Manager con utenza myuser e privilegi di amministratore.

---

📌 *Nota*: Questo setup è pensato per ambienti di test. Per un'installazione in produzione, applicare politiche di sicurezza adeguate ai permessi, alla configurazione di Keycloak e alla gestione delle credenziali.

