# Password vault


#### A single password to remember it all.

- A secure local vault to help you maintain your passwords.
- Write credentials to it safely and carry the vault file everywhere. 
- Retrieve your passwords whenever you want.
- Strong `"AES/CBC/PKCS5Padding"` security.

**The default vault file:** `~/myvault/passwords_${username}.mv.db`

---
# The software

**Downloads Folder:** https://www.dropbox.com/sh/y54c1b6csebxh37/AAAp-TpV7H3hbnBnjHmBW_t3a?dl=0

**Latest Version:** 1.0

---
# Execute

Execute like a regular jar file.
```
 Srini: ~/password_vault (addBuild) => java -jar password_vault-1.0.jar 
```

---
# Developer

## Maven build

Install maven on your machine using https://docs.scala-lang.org/tutorials/scala-with-maven.html

To compile:
```
Srini: ~/password_vault (addBuild) => mvn compile
```

To execute:
```
Srini: ~/password_vault (addBuild) => mvn exec:java
```

To create the release jar:
```
Srini: ~/password_vault (addBuild) => mvn clean compile package assembly:single
```
that creates 
```
 Srini: ~/password_vault (addBuild) => ls -lah target/password_vault-1.0.jar 
-rw-r--r--  1 srinivasan  staff    25M Jul 11 16:47 target/password_vault-1.0.jar
```

For other maven commands, refer its official documentation 

---
### Set up IntelliJ

Install the Maven plugin and enable `Use plugin registry` in settings.

Attached the `password_vault.iml` for reference.

Setup a project from existing sources using this project and then you should 
be ale to do regular build/debug/etc.

---
### Unit tests

Run tests under `VaultTests` either using Maven or via IntelliJ 

