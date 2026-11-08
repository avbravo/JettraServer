# JettraSecurityDB Documentation

`JettraSecurityDB` is a native, lightweight, and high-performance Java Object Database integrated directly within `JettraServer`. It requires no external library dependencies and stores serialized Java objects efficiently in `/db/securitydb` inside the application directory.

---

## Key Features

- **Native Object Storage**: Operates directly on Java objects (specifically `JRole`, `JUser`, and `JCredential` records) using optimized serialization.
- **Repository Pattern**: Communication with the database is abstracted using repository interfaces (`JRoleRepository`, `JUserRepository`, `JCredentialRepository`).
- **Custom Query Language**: Contains a built-in SQL-like parser allowing complex queries on simple, nested, or collection properties of your records.
- **Cloning & Cloud Ready**: Uses double-locking mechanics (JVM-level `ReentrantReadWriteLock` + Process-level NIO `FileLock`) to prevent race conditions or data corruption when multiple server instances run in a cloned cluster or cloud environment.
- **Compact Header**: Serializes a `CompactHeader` before each object to store node details, database version, and write timestamps.
- **Administration Web Console**: A premium, responsive glassmorphic console at `/securitydb/admin` for real-time registry viewing and modifications.

---

## Configuration & Seeding

To run JettraServer as a backend server and initialize the security database, configure [jettra-config.properties](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/JettraServer/src/main/resources/jettra-config.properties):

```properties
server.typebackend=true
```

When this property is `true`, `JettraServer` automatically runs `JettraSecurityDBInitializer` at startup. If the database collections are empty, it populates:
1. **Roles**: `ADMIN`, `MANAGER`, and `DEMO`.
2. **User**: `admin` user linked to the `ADMIN` role.
3. **Credentials**: `admin` credential with a SHA-256 hashed password.

---

## Repository CRUD Usage Examples

### 1. JRoleRepository Example
```java
JRoleRepository roleRepo = new JRoleRepositoryImpl();

// 1. Create and Save
JRole supportRole = new JRole(UUID.randomUUID(), "SUPPORT", true);
roleRepo.save(supportRole);

// 2. Find by ID
Optional<JRole> retrieved = roleRepo.findById(supportRole.id());
retrieved.ifPresent(role -> System.out.println("Role found: " + role.name()));

// 3. Find All
List<JRole> allRoles = roleRepo.findAll();

// 4. Delete
roleRepo.delete(supportRole.id());
```

### 2. JUserRepository Example
```java
JUserRepository userRepo = new JUserRepositoryImpl();
JRoleRepository roleRepo = new JRoleRepositoryImpl();

// Find ADMIN role to assign
Optional<JRole> adminRole = roleRepo.search("name = 'ADMIN'").stream().findFirst();
Set<JRole> roles = new HashSet<>();
adminRole.ifPresent(roles::add);

// Save User
JUser newUser = new JUser(
    UUID.randomUUID(),
    "John",
    "Doe",
    "john.doe@jettra.io",
    "+1234567890",
    true,
    roles
);
userRepo.save(newUser);
```

### 3. JCredentialRepository Example
```java
JCredentialRepository credRepo = new JCredentialRepositoryImpl();
JUserRepository userRepo = new JUserRepositoryImpl();

// Find user by ID
JUser user = userRepo.findAll().get(0);

// Hash password and save credentials
String hashedPassword = JettraSecurityDBInitializer.hashPassword("my_secret_password");
JCredential credential = new JCredential(
    UUID.randomUUID(),
    user,
    "johndoe",
    hashedPassword,
    true,
    null // lastLogin
);
credRepo.save(credential);
```

---

## Custom Query Language Reference

`JettraSecurityDB` provides a custom parser [JettraQueryParser](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/JettraServer/src/main/java/com/jettra/server/db/security/JettraQueryParser.java) to compile SQL-like filter expressions.

### Syntax Specifications:
- **Comparison Operators**: `=`, `==`, `!=`, `contains`, `startsWith`, `endsWith`.
- **Logical Connectives**: `AND`, `OR` (case-insensitive).
- **Parentheses Grouping**: `( ... )`.
- **Nested Object Traversal**: Use dots (e.g. `juser.email`).
- **Collection Traversal**: Match elements inside collections (e.g. `jRoles.name`).

### Query Examples:

#### 1. Match Simple Properties
Find active roles:
```java
List<JRole> activeRoles = roleRepo.search("active = true");
```

#### 2. Match Nested Properties
Find credentials belonging to users named "admin":
```java
List<JCredential> adminCreds = credRepo.search("juser.firstName = 'admin'");
```

#### 3. Match Elements inside Collections
Find users who hold the `ADMIN` role:
```java
List<JUser> administrators = userRepo.search("jRoles.name = 'ADMIN'");
```

#### 4. Logical Operators & Grouping
Evaluate multiple criteria with parentheses:
```java
List<JRole> filtered = roleRepo.search("(name = 'ADMIN' OR name = 'MANAGER') AND active = true");
```

---

## Cloned Clusters Concurrency (OS-level locking)

In cloned or cloud-based clusters, multiple independent Java processes might access the same database folder `/db/securitydb`. `JettraSecurityDB` handles this natively using file channel locks:

```java
// For Writes (Exclusive locking)
fos = new FileOutputStream(file);
java.nio.channels.FileChannel channel = fos.getChannel();
fileLock = channel.lock(); // Process waits here until it obtains the lock

// For Reads (Shared locking)
fis = new FileInputStream(file);
java.nio.channels.FileChannel channel = fis.getChannel();
fileLock = channel.lock(0L, Long.MAX_VALUE, true); // Multiple processes can read simultaneously
```
This double-locking mechanism guarantees full acid-like serialization safety inside cloud/cloned environments.

---

## Administrative Terminal Console (CLI)

`JettraSecurityDB` provides a terminal-based command-line interface (CLI) to manage roles, users, and credentials interactively. It enforces strict user authentication by querying credentials directly from `JettraSecurityDB` and validating password hashes.

### Launching the CLI
To launch the console on a server node during development, execute the following Maven command:

```bash
mvn exec:java -Dexec.mainClass="io.jettra.server.autentification.SecurityCLI"
```

You can also launch it directly from your packaged application by adding support for the `-console` argument in your main class:
```bash
java -jar target/myapplication.jar -console
```

**Implementation Example (`Main.java`):**
```java
public static void main(String[] args) {
    if (args != null && args.length > 0 && args[0].equals("-console")) {
        io.jettra.server.autentification.SecurityCLI.main(args);
        return;
    }
    // Continue with your standard application startup...
}
```

### Features & Operations
1. **Secure Login**: Access is granted only to active users with valid credentials stored in `JettraSecurityDB` (such as the default `admin` user).
2. **Interactive CRUD**:
   - **Manage Roles**: List roles, create roles, toggle role active/inactive status, and **search roles**.
   - **Manage Users**: List users, create users, delete users, toggle active status, **change user password**, and **search users**.
   - **Manage Credentials**: List credentials, create new login credentials, delete credentials, toggle active status, search credentials, and **show microservice credentials** (searches for an existing credential by username and displays it along with an encrypted version using a user-provided secret phrase, without modifying the database).
3. **Audit Tracking**: Successful login updates the `lastLogin` timestamp of the user's credential record in real-time.

### Console Menu Example
When launched, the console asks for authentication and presents an interactive menu:

```text
=================================================
    JettraSecurityDB - Administrative Console CLI
=================================================

Please log in to continue.
Username: admin
Password:
Access Granted! Welcome, admin admin.

=================================
           MAIN MENU             
=================================
1. Manage Roles (JRole)
2. Manage Users (JUser)
3. Manage Credentials (JCredential)
4. Exit Console
=================================
Select an option (1-4, or 'exit'): 2

--- Manage Users ---
1. List All Users
2. Create New User
3. Delete User
4. Toggle User Active Status
5. Change User Password
6. Search Users
7. Back to Main Menu
Select action: 
```
